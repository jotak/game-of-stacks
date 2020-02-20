package demo.gos.villains

import demo.gos.common.*
import demo.gos.common.maths.Point
import io.vertx.core.json.JsonObject
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import io.vertx.kotlin.kafka.client.producer.writeAwait
import java.security.SecureRandom
import java.util.*

const val DELTA_MS = 1000L
const val RANGE = 30.0
val SPEED = Commons.getDoubleEnv("SPEED", 45.0)
// Accuracy [0, 1]
val ACCURACY = Commons.getDoubleEnv("ACCURACY", 0.7)
val RND = SecureRandom()

class Villain(private val kafkaProducer: KafkaProducer<String, JsonObject>) {

  private val id = "V-${UUID.randomUUID()}"
  private var pos = Areas.spawnVillainsArea.spawn(RND)
  private var randomDest: Point? = null
  private var target: PerceivedNoise? = null
  private var silentTargetTimer: Gauge? = null
  private val maxLifeTimer = Gauge(60.0, fun() { isDead = true })
  private val deadTimer = Gauge(3.0, fun() { garbage = true })
  var isDead = false
  var garbage = false

  // listen to heroes noise to detect if a good target is available
  fun listenToHeroes(noise: Noise) {
    val noisePos = noise.toPoint()
    val perceived = PerceivedNoise.create(noise, pos)
    val currentTarget = target

    if (currentTarget == null) {
      LOGGER.info("A Villain has elected a target at $noisePos")
      target = perceived
      silentTargetTimer = Gauge(3.0, fun() { target = null })
    } else {
      if (noise.id == currentTarget.noise.id) {
        // Update target position
        target = perceived
        silentTargetTimer?.reset()
      } else {
        // 40% chances to get attention
        if (perceived.isStrongerThan(currentTarget) && RND.nextInt(100) < 40) {
          LOGGER.info("A Villain has elected a different target at $noisePos")
          target = perceived
          silentTargetTimer?.reset()
        }
      }
    }
  }

  fun onKillAround(zone: Circle) {
    if (isDead) {
      return
    }
    if (zone.contains(pos)) {
      LOGGER.info("Aaaarrrrhggg!!!! (Today, a villain has died)")
      isDead = true
    }
  }

  fun onKillSingle(killed: String) {
    if (isDead) {
      return
    }
    if (id == killed) {
      LOGGER.info("Aaaarrrrhggg!!!! (Today, a villain has died)")
      isDead = true
    } else if (target?.noise?.id == killed) {
      target = null
    }
  }

  suspend fun update(delta: Double, isPaused: Boolean) {
    if (isDead) {
      deadTimer.add(delta)
    } else if (!isPaused) {
      maxLifeTimer.add(delta)
      silentTargetTimer?.add(delta)
      target?.fade()
      val t = target
      if (t != null) {
        pos = Players.walk(RND, pos, t.noise.toPoint(), SPEED, ACCURACY, delta)
        // If destination reached => kill target
        if (Circle.fromCenter(pos, RANGE).contains(t.noise.toPoint())) {
          // TODO: broadcasting maybe not necessary here?
          kotlin.runCatching {
            kafkaProducer.writeAwait(KafkaProducerRecord.create("kill-single", JsonObject().put("id", t.noise.id)))
          }.onFailure {
            LOGGER.error("Kill error", it)
          }
          // Villain dies, too
          isDead = true
        }
      } else {
        val positions = Players.walkRandom(RND, pos, randomDest, SPEED, ACCURACY, delta)
        pos = positions.first
        randomDest = positions.second
      }
      kotlin.runCatching {
        kafkaProducer.writeAwait(KafkaProducerRecord.create("villain-making-noise", JsonObject.mapFrom(Noise.fromPoint(id, pos))))
      }.onFailure {
        LOGGER.error("Make noise error", it)
      }
    }
  }

  fun getDisplayData(): DisplayData {
    val sprite = if (isDead) "rip" else "white-walker"
    return DisplayData(
      id = id,
      x = pos.x(),
      y = pos.y(),
      sprite = sprite
    )
  }
}
