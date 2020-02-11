package demo.gos.villains

import demo.gos.common.*
import demo.gos.common.Gauge
import demo.gos.common.maths.Point
import io.vertx.core.json.JsonObject
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import io.vertx.kotlin.kafka.client.producer.writeAwait
import java.security.SecureRandom
import java.util.*

const val DELTA_MS = 1000L
const val RANGE = 30.0
val SPEED = Commons.getDoubleEnv("SPEED", 35.0)
// Accuracy [0, 1]
val ACCURACY = Commons.getDoubleEnv("ACCURACY", 0.7)
val RND = SecureRandom()

class Villain(private val kafkaProducer: KafkaProducer<String, JsonObject>) {

  private val id = "V-${UUID.randomUUID()}"
  private var pos = Areas.spawnVillainsArea.spawn(RND)
  private var randomDest: Point? = null
  private var target: Noise? = null
  private var targetCountDown = 0
  var isDead = false
  private var isPaused = false
  var stopped = false
  private val deadTimer = Gauge(3.0, fun() { stop() })
  private val maxLifeTimer = Gauge(60.0, fun() { stop() })

  fun onGameControls(json: JsonObject) {
    when (json.getString("type")) {
      "play" -> isPaused = false
      "pause" -> isPaused = true
    }
  }

  fun stop() {
    stopped = true
  }

  // listen to heroes noise to detect if a good target is available
  fun listenToHeroes(json: JsonObject) {
    val noise = json.mapTo(Noise::class.java)
    val noisePos = noise.toPoint()
    val currentTarget = target

    if (currentTarget == null) {
      LOGGER.info("A Villain has elected a target at $noisePos")
      target = noise
    } else {
      if (noise.id == currentTarget.id) {
        // Update target position
        target = noise
        targetCountDown = 3
      } else {
        val currentStrength = currentTarget.strength(pos)
        val newStrength = noise.strength(pos)
        // 5% chances to get attention
        if (newStrength > currentStrength && RND.nextInt(100) < 5) {
          LOGGER.info("A Villain has elected a different target at $noisePos")
          target = noise
        }
      }
    }
  }

  fun onKillAround(json: JsonObject) {
    if (isDead) {
      return
    }
    val zone = json.mapTo(Circle::class.java)
    if (zone.contains(pos)) {
      LOGGER.info("Aaaarrrrhggg!!!! (Today, a villain has died)")
      isDead = true
    }
  }

  fun onKillSingle(json: JsonObject) {
    if (isDead) {
      return
    }
    if (id == json.getString("id")) {
      LOGGER.info("Aaaarrrrhggg!!!! (Today, a villain has died)")
      isDead = true
    }
  }

  suspend fun update(delta: Double) {
    if (isDead) {
      deadTimer.add(delta)
    } else if (!isPaused) {
      val t = target
      if (t != null) {
        pos = Players.walk(RND, pos, t.toPoint(), SPEED, ACCURACY, delta)
        // If destination reached => kill target
        if (Circle.fromCenter(pos, RANGE).contains(t.toPoint())) {
          // TODO: broadcasting maybe not necessary here?
          kotlin.runCatching {
            kafkaProducer.writeAwait(KafkaProducerRecord.create("kill-single", JsonObject().put("id", t.id)))
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
    }
    display()
    if (targetCountDown >= 0) {
      targetCountDown--
    } else if (target != null) {
      LOGGER.info("A villain had no sign from the current target for a while")
      target = null
    }
    maxLifeTimer.add(delta)
  }

  private suspend fun display() {
    val sprite = if (isDead) "rip" else "white-walker"
    val data = DisplayData(
      id = id,
      x = pos.x(),
      y = pos.y(),
      sprite = sprite
    )
    val json = JsonObject.mapFrom(data)
    kotlin.runCatching {
      kafkaProducer.writeAwait(KafkaProducerRecord.create("display", json))
      if (!isDead) {
        kafkaProducer.writeAwait(KafkaProducerRecord.create("villain-making-noise", JsonObject.mapFrom(Noise.fromPoint(id, pos))))
      }
    }.onFailure {
      LOGGER.error("Display error", it)
    }
  }
}
