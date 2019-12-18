package demo.gos.villains

import demo.gos.common.Commons
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.coroutines.CoroutineVerticle

val LOGGER: Logger = LoggerFactory.getLogger("Villains")
val WAVES_SIZE = Commons.getIntEnv("WAVES_SIZE", 10)
val WAVES_DELAY = Commons.getDoubleEnv("WAVES_DELAY", 10.0)

class VillainsVerticle : CoroutineVerticle() {
  private var waveTimer = 0.0
  private var counter = 0

  override suspend fun start() {
    // Waves scheduler
    vertx.setPeriodic(500) {
      if (WAVES_SIZE > 0) {
        waveTimer -= 0.5
        if (waveTimer <= 0) {
          waveTimer = WAVES_DELAY
          createVillains(WAVES_SIZE)
        }
      }
    }
  }

  private fun createVillains(size: Int) {
    LOGGER.info("New villains wave!")
    if (counter > 100) {
      LOGGER.info("(Skipping)")
      return
    }
    (0 until size).forEach { _ -> Villain(vertx) }
    counter += size
  }
}
