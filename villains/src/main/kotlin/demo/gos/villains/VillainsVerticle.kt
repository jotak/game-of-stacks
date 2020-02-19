package demo.gos.villains

import demo.gos.common.Circle
import demo.gos.common.Commons
import demo.gos.common.Noise
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.kafka.client.consumer.KafkaConsumer
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.kafka.client.producer.writeAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val LOGGER: Logger = LoggerFactory.getLogger("Villains")
val WAVES_SIZE = Commons.getIntEnv("WAVES_SIZE", 10)
val WAVES_COUNT = Commons.getIntEnv("WAVES_COUNT", 10)
val WAVES_DELAY = Commons.getDoubleEnv("WAVES_DELAY", 20.0)

class VillainsVerticle : CoroutineVerticle() {
  private var waveTimer = 0.0
  private var wavesCount = 0
  private var paused = false
  private var ended = false
  private val villains = mutableListOf<Villain>()
  private var gameLoopId: Long? = null
  private var heroesCountDown = -1.0

  override suspend fun start() {
    val kafkaProducer = KafkaProducer.create<String, JsonObject>(vertx, Commons.kafkaConfigProducer)
    val kafkaDisplayProducer = KafkaProducer.create<String, JsonArray>(vertx, Commons.kafkaArrayConfigProducer)
    // Waves scheduler
    gameLoopId = vertx.setPeriodic(DELTA_MS) {
      GlobalScope.launch(vertx.dispatcher()) {
        update(DELTA_MS.toDouble() / 1000.0, kafkaProducer, kafkaDisplayProducer)
      }
    }

    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer("villains"))
      .subscribe("controls").handler { onGameControls(it.value()) }
    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer("villains"))
      .subscribe("gameover").handler { ended = true }
    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer("villains"))
      .subscribe("hero-making-noise").handler { listenToHeroes(it.value()) }
    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer("villains"))
      .subscribe("kill-around").handler { onKillAround(it.value()) }
    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer("villains"))
      .subscribe("kill-single").handler { onKillSingle(it.value()) }
  }

  private suspend fun update(delta: Double, kafkaProducer: KafkaProducer<String, JsonObject>, kafkaDisplayProducer: KafkaProducer<String, JsonArray>) {
    villains.removeIf { it.garbage }
    villains.forEach { it.update(delta, paused) }
    val displayData = villains.map { it.getDisplayData() }

    kotlin.runCatching {
      kafkaDisplayProducer.writeAwait(KafkaProducerRecord.create("display", JsonArray(displayData)))
    }.onFailure {
      LOGGER.error("Display error", it)
    }

    if (ended || paused) {
      return
    }

    // Waves
    if (wavesCount < WAVES_COUNT) {
      waveTimer -= delta
      if (waveTimer <= 0) {
        wavesCount++
        waveTimer = WAVES_DELAY
        (0 until WAVES_SIZE).forEach { _ -> villains.add(Villain(kafkaProducer)) }
        LOGGER.info("New villains wave! now: ${villains.size}")
      }
    }

    // Check game end
    if (heroesCountDown > 0) {
      heroesCountDown -= delta
      if (heroesCountDown <= 0) {
        kafkaProducer.writeAwait(KafkaProducerRecord.create("gameover", JsonObject().put("winner", "villains")))
      } else if (wavesCount == WAVES_COUNT && alive().isEmpty()) {
        kafkaProducer.writeAwait(KafkaProducerRecord.create("gameover", JsonObject().put("winner", "heroes")))
      }
    }
  }

  private fun onGameControls(json: JsonObject) {
    println("onGameControls: $json")
    when (json.getString("type")) {
      "play" -> paused = false
      "pause" -> paused= true
      "reset" -> reset()
    }
  }

  private fun onKillAround(json: JsonObject) {
    val zone = json.mapTo(Circle::class.java)
    alive().forEach { it.onKillAround(zone) }
  }

  private fun onKillSingle(json: JsonObject) {
    val killed = json.getString("id")
    alive().forEach { it.onKillSingle(killed) }
  }

  private fun listenToHeroes(json: JsonObject) {
    heroesCountDown = 10.0
    val noise = json.mapTo(Noise::class.java)
    alive().forEach { it.listenToHeroes(noise) }
  }

  private fun alive() = villains.filter { !it.isDead }

  private fun reset() {
    villains.clear()
    paused = false
    waveTimer = 0.0
    wavesCount = 0
    heroesCountDown = -1.0
    ended = false
  }
}
