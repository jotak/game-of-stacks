package demo.gos.villains

import demo.gos.common.Commons
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.kafka.client.consumer.KafkaConsumer
import io.vertx.kotlin.coroutines.CoroutineVerticle


val LOGGER: Logger = LoggerFactory.getLogger("Villains")
val WAVES_SIZE = Commons.getIntEnv("WAVES_SIZE", 10)
val WAVES_DELAY = Commons.getDoubleEnv("WAVES_DELAY", 20.0)

class VillainsVerticle : CoroutineVerticle() {
  private var waveTimer = 0.0
  private var isPaused = false

  override suspend fun start() {


    // Waves scheduler
    vertx.setPeriodic(500) {
      if (WAVES_SIZE > 0 && !isPaused) {
        waveTimer -= 0.5
        if (waveTimer <= 0) {
          waveTimer = WAVES_DELAY
          LOGGER.info("New villains wave!")
          (0 until WAVES_SIZE).forEach { _ -> Villain(vertx) }
        }
      }
    }

    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer("villains"))
      .subscribe("game").handler { onGameControls(it.value()) }
  }

  private fun onGameControls(json: JsonObject) {
    when (json.getString("type")) {
      "play" -> isPaused = false
      "pause" -> isPaused = true
      "reset" -> reset()
    }
  }

  private fun reset() {
    isPaused = false
    waveTimer = 0.0
  }
}
