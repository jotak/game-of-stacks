package demo.gos.villains

import demo.gos.common.*
import demo.gos.common.Gauge
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
const val RANGE = 30.0
val SPEED = Commons.getDoubleEnv("SPEED", 35.0)
// Accuracy [0, 1]
val ACCURACY = Commons.getDoubleEnv("ACCURACY", 0.7)
val RND = SecureRandom()

class Villain(private val vertx: Vertx) {
  private val kafkaProducer = KafkaProducer.create<String, JsonObject>(vertx, Commons.kafkaConfigProducer)
  private val id = "V-${UUID.randomUUID()}"
  private var pos = Areas.spawnVillainsArea.spawn(RND)
  private var randomDest: Point? = null
  private var target: Noise? = null
  private var isDead = false
  private var isPaused = false
  private val deadTimer = Gauge(3.0, fun() { stop() })
  private val maxLifeTimer = Gauge(60.0, fun() { stop() })
  private val gameLoopId: Long
  private val consumers = mutableListOf<KafkaConsumer<String, JsonObject>>()

  init {
    newConsumer().subscribe("game").handler { onGameControls(it.value()) }
    newConsumer().subscribe("hero-making-noise").handler { listenToHeroes(it.value()) }
    newConsumer().subscribe("kill-around").handler { onKillAround(it.value()) }
    newConsumer().subscribe("kill-single").handler { onKillSingle(it.value()) }

    // Start game loop
    gameLoopId = vertx.setPeriodic(DELTA_MS) {
      GlobalScope.launch(vertx.dispatcher()) {
        update(DELTA_MS.toDouble() / 1000.0)
      }
    }
  }

  private fun newConsumer(): KafkaConsumer<String, JsonObject> {
    val c = KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer(id))
    consumers.add(c)
    return c
  }

  private fun stop() {
    vertx.cancelTimer(gameLoopId)
    consumers.forEach { it.unsubscribe() }
    consumers.clear()
  }

  private fun onGameControls(json: JsonObject) {
    when (json.getString("type")) {
      "play" -> isPaused = false
      "pause" -> isPaused = true
      "reset" -> stop()
    }
  }

  // listen to heroes noise to detect if a good target is available
  private fun listenToHeroes(json: JsonObject) {
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

  private fun onKillAround(json: JsonObject) {
    if (isDead) {
      return
    }
    val zone = json.mapTo(Circle::class.java)
    if (zone.contains(pos)) {
      LOGGER.info("Aaaarrrrhggg!!!! (Today, a villain has died)")
      isDead = true
    }
  }

  private fun onKillSingle(json: JsonObject) {
    if (isDead) {
      return
    }
    if (id == json.getString("id")) {
      LOGGER.info("Aaaarrrrhggg!!!! (Today, a villain has died)")
      isDead = true
    }
  }

  private suspend fun update(delta: Double) {
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
    maxLifeTimer.add(delta)
  }

  private suspend fun display() {
    val color = if (isDead) "#110b32" else "#311b92"
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
