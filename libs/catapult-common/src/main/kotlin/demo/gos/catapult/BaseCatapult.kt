package demo.gos.catapult

import demo.gos.common.*
import demo.gos.common.maths.Point
import demo.gos.common.maths.Segment
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.atan
import kotlin.math.min
import kotlin.math.tan

val X = System.getenv("X")?.toDouble()
val Y = System.getenv("Y")?.toDouble()
val LOAD_FACTOR = Commons.getDoubleEnv("LOAD_FACTOR", 0.01)
// Accuracy [0, 1]
val ACCURACY = Commons.getDoubleEnv("ACCURACY", 0.9)
val SHOT_RANGE = Commons.getDoubleEnv("SHOT_RANGE", 400.0)
val SPEED = Commons.getDoubleEnv("SPEED", 110.0)
val IMPACT_ZONE = Commons.getDoubleEnv("IMPACT_ZONE", 75.0)


abstract class BaseCatapult(private val colorize: (Double) -> String) {
  protected var id: String = UUID.randomUUID().toString();
  protected val rnd = SecureRandom()
  private val pos = GameObjects.startingPoint(rnd, Areas.spawnWeaponArea, X, Y)
  private var boulders = mutableListOf<BaseBoulder>()
  private var target: PerceivedNoise? = null
  private var gauge = Gauge(1.0, fun() { shoot() })
  private var isLoading = AtomicBoolean(false)
  private val computedValue = AtomicReference(0.0)
  private val throughput = Throughput.Printer("Load")
  private var throughputSuccess = Throughput.Printer("Success")

  protected suspend fun update(delta: Double) {
    target?.fade()
    makeNoise(Noise.fromPoint(id, pos))
    val newBoulders = boulders.filter { !it.update(delta) }
    boulders = newBoulders.toMutableList()
    display()
  }

  abstract suspend fun makeNoise(noise: Noise)

  fun load(v: Double?): Double? {
    throughput.mark()
    if (isLoading.getAndSet(true)) {
      println("Loading aborted (not ready)")
      return null
    }
    // Do something CPU intensive
    val d = tan(atan(tan(atan(v ?: 0.0))))
    computedValue.set(d)
    gauge.add(LOAD_FACTOR)
    isLoading.set(false)
    throughputSuccess.mark()
    return gauge.get()
  }

  // listen to noise; when target seems more interesting than the current one, take it instead
  protected fun listenToVillains(noise: Noise) {
    val perceived = PerceivedNoise.create(noise, pos)
    val currentTarget = target
    if (currentTarget == null) {
      target = perceived
    } else {
      if (noise.id == currentTarget.noise.id) {
        // Update target position
        target = perceived
      } else {
        // 10% chances to get attention
        if (perceived.isStrongerThan(currentTarget) && rnd.nextInt(100) < 10) {
          target = perceived
        }
      }
    }
  }

  private fun shoot() {
    val t = target
    if (t != null) {
      // Accuracy modifier
      var angle = rnd.nextDouble() * (1.0 - ACCURACY) * Math.PI
      if (rnd.nextInt(2) == 0) {
        angle *= -1
      }
      // Is in range?
      val segToDest = Segment(pos, t.noise.toPoint())
      val shootSize = min(segToDest.size(), SHOT_RANGE)
      val dest = segToDest.derivate().normalize().rotate(angle).mult(shootSize).add(pos)
      val boulder = createBoulder("$id-BOULDER", pos, dest, SPEED, IMPACT_ZONE)
      boulders.add(boulder)
      target = null
    }
  }

  abstract fun createBoulder(id: String, pos: Point, dest: Point, speed: Double, impact: Double): BaseBoulder

  abstract suspend fun display(data: DisplayData)

  private suspend fun display() {
    display(DisplayData(id = id, x = pos.x(), y = pos.y(), sprite = "catapult", value = gauge.get() ))
  }
}
