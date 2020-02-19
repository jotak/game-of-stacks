package demo.gos.villains

import demo.gos.common.Commons
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
import java.util.*

val LOGGER: Logger = LoggerFactory.getLogger("Villains")
val WAVES_SIZE = Commons.getIntEnv("WAVES_SIZE", 10)
val WAVES_COUNT = Commons.getIntEnv("WAVES_COUNT", 10)
val WAVES_DELAY = Commons.getDoubleEnv("WAVES_DELAY", 20.0)

class VillainsVerticle : CoroutineVerticle() {
  private var waveTimer = 0.0
  private var wavesCount = 0
  private var isPaused = false
  private var ended = false
  private val consumers = mutableListOf<KafkaConsumer<String, JsonObject>>()
  private val villains = mutableListOf<Villain>()
  private var gameLoopId: Long? = null
  private var heroesCountDown = -1.0

  override suspend fun start() {
    val kafkaProducer = KafkaProducer.create<String, JsonObject>(vertx, Commons.kafkaConfigProducer)
    // Waves scheduler
    gameLoopId = vertx.setPeriodic(DELTA_MS) {
      if (!isPaused) {
        GlobalScope.launch(vertx.dispatcher()) {
          update(DELTA_MS.toDouble() / 1000.0, kafkaProducer)
        }
      }
    }

    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer("villains"))
      .subscribe("game").handler { onGameControls(it.value()) }

    newConsumer().subscribe("hero-making-noise").handler { listenToHeroes(it.value()) }
    newConsumer().subscribe("kill-around").handler { onKillAround(it.value()) }
    newConsumer().subscribe("kill-single").handler { onKillSingle(it.value()) }
  }

  private suspend fun update(delta: Double, kafkaProducer: KafkaProducer<String, JsonObject>) {
    runningVillains().forEach {
      it.update(delta)
    }

    if (ended) {
      return
    }
    // Waves
    if (wavesCount < WAVES_COUNT) {
      waveTimer -= delta
      if (waveTimer <= 0) {
        wavesCount++
        waveTimer = WAVES_DELAY
        (0 until WAVES_SIZE).forEach { _ -> villains.add(Villain(kafkaProducer)) }
        LOGGER.info("New villains wave! now: ${runningVillains().size}")
      }
    }

    // Check game end
    if (heroesCountDown > 0) {
      heroesCountDown -= delta
      if (heroesCountDown <= 0) {
        kafkaProducer.writeAwait(KafkaProducerRecord.create("game", JsonObject().put("type", "end").put("winner", "villains")))
      } else if (wavesCount == WAVES_COUNT && runningVillains().count { !it.isDead } == 0) {
        kafkaProducer.writeAwait(KafkaProducerRecord.create("game", JsonObject().put("type", "end").put("winner", "heroes")))
      }
    }
  }

  private fun newConsumer(): KafkaConsumer<String, JsonObject> {
    val c = KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer(UUID.randomUUID().toString()))
    consumers.add(c)
    return c
  }

  private fun onGameControls(json: JsonObject) {
    println("onGameControls: $json")
    runningVillains().forEach { it.onGameControls(json) }
    when (json.getString("type")) {
      "play" -> isPaused = false
      "pause" -> isPaused = true
      "reset" -> reset()
      "end" -> ended = true
    }
  }

  private fun onKillAround(json: JsonObject) {
    runningVillains().forEach { it.onKillAround(json) }
  }

  private fun onKillSingle(json: JsonObject) {
    runningVillains().forEach { it.onKillSingle(json) }
  }

  private fun listenToHeroes(json: JsonObject) {
    heroesCountDown = 10.0
    runningVillains().forEach { it.listenToHeroes(json) }
  }

  private fun runningVillains() = villains.filter { !it.stopped }

  private fun reset() {
    consumers.forEach { it.unsubscribe() }
    consumers.clear()
    villains.clear()
    isPaused = false
    waveTimer = 0.0
    wavesCount = 0
    heroesCountDown = -1.0
    ended = false
  }
}
