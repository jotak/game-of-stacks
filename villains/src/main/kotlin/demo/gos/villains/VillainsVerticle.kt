package demo.gos.villains

import demo.gos.common.Commons
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.kafka.client.consumer.KafkaConsumer
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


val LOGGER: Logger = LoggerFactory.getLogger("Villains")
val WAVES_SIZE = Commons.getIntEnv("WAVES_SIZE", 10)
val WAVES_DELAY = Commons.getDoubleEnv("WAVES_DELAY", 20.0)

class VillainsVerticle : CoroutineVerticle() {
  private var kafkaProducer: KafkaProducer<String, JsonObject>? = null
  private var waveTimer = 0.0
  private var isPaused = false
  private val consumers = mutableListOf<KafkaConsumer<String, JsonObject>>()
  private val villains = mutableListOf<Villain>()
  private var gameLoopId: Long? = null
  private var waveScheduler: Long? = null

  override suspend fun start() {
    kafkaProducer = KafkaProducer.create<String, JsonObject>(vertx, Commons.kafkaConfigProducer)
    // Waves scheduler
    waveScheduler = vertx.setPeriodic(500) {
      if (WAVES_SIZE > 0 && !isPaused) {
        waveTimer -= 0.5
        if (waveTimer <= 0) {
          waveTimer = WAVES_DELAY
          LOGGER.info("New villains wave! now: ${runningVillains().size}")
          (0 until WAVES_SIZE).forEach { _ -> villains.add(Villain(kafkaProducer!!)) }
        }
      }
    }

    gameLoopId = vertx.setPeriodic(DELTA_MS) {
      if (!isPaused) {
        update(DELTA_MS.toDouble() / 1000.0)
      }
    }

    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer("villains"))
      .subscribe("game").handler { onGameControls(it.value()) }

    newConsumer().subscribe("hero-making-noise").handler { listenToHeroes(it.value()) }
    newConsumer().subscribe("kill-around").handler { onKillAround(it.value()) }
    newConsumer().subscribe("kill-single").handler { onKillSingle(it.value()) }
  }

  private fun update(delta: Double) {
    runningVillains().forEach {
      GlobalScope.launch(vertx.dispatcher()) {
        it.update(delta)
      }
    }
  }

  private fun newConsumer(): KafkaConsumer<String, JsonObject> {
    val c = KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer(UUID.randomUUID().toString()))
    consumers.add(c)
    return c
  }

  private fun onGameControls(json: JsonObject) {
    runningVillains().forEach { it.onGameControls(json) }
    when (json.getString("type")) {
      "play" -> isPaused = false
      "pause" -> isPaused = true
      "reset" -> reset()
    }

  }

  private fun onKillAround(json: JsonObject) {
    runningVillains().forEach { it.onKillAround(json) }
  }

  private fun onKillSingle(json: JsonObject) {
    runningVillains().forEach { it.onKillSingle(json) }
  }

  private fun listenToHeroes(json: JsonObject) {
    runningVillains().forEach { it.listenToHeroes(json) }
  }

  private fun runningVillains() = villains.filter { !it.stopped }

  private fun reset() {
    consumers.forEach { it.unsubscribe() }
    consumers.clear()
    villains.clear()
    isPaused = false
    waveTimer = 0.0
  }
}
