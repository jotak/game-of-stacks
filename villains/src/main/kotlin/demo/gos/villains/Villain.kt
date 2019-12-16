package demo.gos.villains

import demo.gos.common.Areas
import demo.gos.common.Commons
import demo.gos.common.Noise
import demo.gos.common.Players
import demo.gos.common.maths.Point
import demo.gos.common.maths.Segment
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kafka.client.consumer.KafkaConsumer
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.kafka.client.producer.writeAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.*

const val DELTA_MS = 200L
const val RANGE = 30
val SPEED = Commons.getDoubleEnv("SPEED", 35.0)
// Accuracy [0, 1]
val ACCURACY = Commons.getDoubleEnv("ACCURACY", 0.7)
val RND = SecureRandom()

class Villain(vertx: Vertx) {
  private val kafkaProducer = KafkaProducer.create<String, JsonObject>(vertx, Commons.kafkaConfigProducer)
  private val id = "V-${UUID.randomUUID()}"
  private var pos = Areas.spawnVillainsArea.spawn(RND)
  private var randomDest: Point? = null
  private var target: Point? = null
  private var isDead = false

  init {
    // TODO: play/pause/reset
    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer)
      .subscribe("hero-making-noise").handler { listenToHeroes(it.value()) }

    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer)
      .subscribe("kill-around").handler { killAround(it.value()) }

    // Start game loop
    vertx.setPeriodic(DELTA_MS) {
      GlobalScope.launch(vertx.dispatcher()) {
        update(DELTA_MS.toDouble() / 1000.0)
      }
    }
  }

  // listen to heroes noise to detect if a good target is available
  private fun listenToHeroes(json: JsonObject) {
  }

  private fun killAround(json: JsonObject) {
    LOGGER.info("Kill event received")
    val impact = Point(json.getDouble("x"), json.getDouble("y"))
    val r = json.getDouble("r")
    if (Segment(pos, impact).size() <= r) {
      LOGGER.info("Killed!!!!")
      LOGGER.info("Today, a villain has died")
      isDead = true
    }
  }

  private suspend fun update(delta: Double) {
    if (!isDead) {
      val dest = target ?: pickRandomDest()
      pos = Players.walk(RND, pos, dest, SPEED, ACCURACY, delta)
      if (isOnTarget()) {
        // TODO: Kill / kamikaze
      } else {
        // TODO: move
      }
    }
    display()
  }

  private fun pickRandomDest(): Point {
    val rd = randomDest
    if (rd != null && pos.diff(randomDest).size() > RANGE) {
      // Not arrived yet => continue to walk to previously picked random destination
      return rd
    }
    // Else, pick a new random destination close to current position
    val newRd = Point(RND.nextDouble() * 100, RND.nextDouble() * 100).diff(Point(50.0, 50.0)).add(pos)
    randomDest = newRd
    return newRd
  }

  private fun isOnTarget(): Boolean {
    val t = target
    return t != null && t.diff(pos).size() <= RANGE
  }

  private suspend fun display() {
    val color = if (isDead) "#101030" else "#802020"
    val json = JsonObject()
      .put("id", id)
      .put("style", "position: absolute; background-color: $color; transition: top ${DELTA_MS}ms, left ${DELTA_MS}ms; height: 30px; width: 30px; border-radius: 50%; z-index: 8;")
      .put("text", "")
      .put("x", pos.x() - 15)
      .put("y", pos.y() - 15)

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
