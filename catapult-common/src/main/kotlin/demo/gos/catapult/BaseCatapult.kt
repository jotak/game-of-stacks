package demo.gos.catapult

import demo.gos.common.*
import demo.gos.common.maths.Point
import demo.gos.common.maths.Segment
import java.security.SecureRandom
import kotlin.math.atan
import kotlin.math.min
import kotlin.math.tan

const val DELTA_MS: Long = 200
val LOAD_FACTOR = Commons.getDoubleEnv("LOAD_FACTOR", 0.1)
// Accuracy [0, 1]
val ACCURACY = Commons.getDoubleEnv("ACCURACY", 0.9)
val SHOT_RANGE = Commons.getDoubleEnv("SHOT_RANGE", 400.0)
//val DETERMINIST = Commons.getStringEnv("DETERMINIST", "false") == "true"
val SPEED = Commons.getDoubleEnv("SPEED", 110.0)
val IMPACT_ZONE = Commons.getDoubleEnv("IMPACT_ZONE", 75.0)
val RND = SecureRandom()

abstract class BaseCatapult(private val id: String) {
  private val pos = Areas.spawnWeaponArea.spawn(RND)
  private var boulders = mutableListOf<BaseBoulder>()
  private var target: Noise? = null
  private var gauge = 0.0
  private var isPaused = false

  protected fun onGameCommand(command: GameCommand) {
    when (command.type) {
      "play" -> isPaused = false
      "pause" -> isPaused = true
      "reset" -> reset()
    }
  }

  private fun reset() {
    isPaused = false
    gauge = 0.0
    target = null
    boulders = mutableListOf()
  }

  protected suspend fun update(delta: Double) {
    if (!isPaused) {
      makeNoise(Noise.fromPoint(id, pos))
      val newBoulders = boulders.filter { !it.update(delta) }
      boulders = newBoulders.toMutableList()
    }
    display()
  }

  abstract suspend fun makeNoise(noise: Noise)

  fun load(v: Double?): String {
    if (isPaused) {
      return "no (I'm paused)"
    }
    // Do something CPU intensive
    val d = tan(atan(tan(atan(v ?: 0.0))))
    gauge += LOAD_FACTOR
    if (gauge >= 1.0) {
      // Shoot!
      shoot()
      gauge = 0.0
      target = null
    }
    return d.toString()
  }

  // listen to noise; when target seems more interesting than the current one, take it instead
  protected fun listenToVillains(noise: Noise) {
    val noisePos = noise.toPoint()
    val currentTarget = target
    if (currentTarget == null) {
      target = noise
    } else {
      if (noise.id == currentTarget.id) {
        // Update target position
        target = noise
      } else {
        val currentDist = Segment(pos, currentTarget.toPoint()).size()
        val newDist = Segment(pos, noisePos).size()
        // 5% chances to get attention
        if (newDist < currentDist && RND.nextInt(100) < 5) {
          target = noise
        }
      }
    }
  }

  private fun shoot() {
    val t = target
    if (t != null) {
      // Accuracy modifier
      var angle = RND.nextDouble() * (1.0 - ACCURACY) * Math.PI
      if (RND.nextInt(2) == 0) {
        angle *= -1
      }
      // Is in range?
      val segToDest = Segment(pos, t.toPoint())
      val shootSize = min(segToDest.size(), SHOT_RANGE)
      val dest = segToDest.derivate().normalize().rotate(angle).mult(shootSize).add(pos)
      val boulder = createBoulder(pos, dest, SPEED, IMPACT_ZONE)
      boulders.add(boulder)
    }
  }

  abstract fun createBoulder(pos: Point, dest: Point, speed: Double, impact: Double): BaseBoulder

  abstract suspend fun display(data: DisplayData)

  private suspend fun display() {
    val red = (gauge * 255).toInt()
    val style = "position: absolute; background-color: rgb($red,128,128); height: 50px; width: 50px; z-index: 7;"
    display(DisplayData(id, pos.x() - 25, pos.y() - 25, style, ""))
  }
}
