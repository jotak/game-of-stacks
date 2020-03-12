package demo.gos.catapult

import demo.gos.common.*
import demo.gos.common.maths.Point
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.sync.Sync
import io.vertx.kafka.client.consumer.KafkaConsumer
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import io.vertx.kotlin.core.executeBlockingAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.kafka.client.producer.writeAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

val LOGGER: Logger = LoggerFactory.getLogger("Catapult-Vertx")
val RUNTIME = Commons.getStringEnv("RUNTIME", "u")
// const val PORT = 8889
const val DELTA_MS: Long = 200
val colorize = fun(gauge: Double): String {
  val red = (gauge * 255).toInt()
  return "rgb($red,128,128)"
}

class CatapultVerticle : CoroutineVerticle() {

  private lateinit var cata: Catapult

  override suspend fun start() {
    cata = Catapult(vertx)

//    val router = Router.router(vertx)
//    router.get("/load").handler { GlobalScope.launch(vertx.dispatcher()) { cata.synchronizer.handle(it.request().getParam("val")?.toDoubleOrNull()) } }

//    vertx.createHttpServer().requestHandler(router).listenAwait(PORT)
//    LOGGER.info("HTTPS server started on port $PORT")
  }
}

class Catapult(private val vertx: Vertx)
  : BaseCatapult(demo.gos.catapult.colorize) {

  private val kafkaProducer = KafkaProducer.create<String, JsonObject>(vertx, Commons.kafkaConfigProducer)
  private val kafkaProducerDisplay = KafkaProducer.create<String, JsonArray>(vertx, Commons.kafkaArrayConfigProducer)

  init {
    id = "CATAPULT-VERTX-${RUNTIME}-${Players.randomName(rnd)}"

    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer(id))
      .subscribe("villain-making-noise").handler { listenToVillains(it.value().mapTo(Noise::class.java)) }

    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer(id))
      .subscribe("load-weapon").handler(Sync.fiberHandler { record ->
        val value = record.value()
        if (value.getString("id") == id) {
          GlobalScope.launch(vertx.dispatcher()) {
            loadSyncCatapult(value.getDouble("val"))
          }
        }
      })

    vertx.setPeriodic(DELTA_MS) {
      GlobalScope.launch(vertx.dispatcher()) {
        update(DELTA_MS.toDouble() / 1000.0)
      }
    }
  }

  private suspend fun loadSyncCatapult(d: Double?): Double? {
    return vertx.executeBlockingAwait {
      it.complete(load(d))
    }
  }

  override suspend fun makeNoise(noise: Noise) {
    kotlin.runCatching {
      kafkaProducer.writeAwait(KafkaProducerRecord.create("weapon-making-noise", JsonObject.mapFrom(noise)))
    }.onFailure {
      LOGGER.error("Noise error", it)
    }
  }

  override fun createBoulder(id: String, pos: Point, dest: Point, speed: Double, impact: Double): BaseBoulder {
    return Boulder(id, vertx, pos, dest, speed, impact)
  }

  override suspend fun display(data: DisplayData) {
    kotlin.runCatching {
      kafkaProducerDisplay.writeAwait(KafkaProducerRecord.create("display", JsonArray(listOf(data))))
    }.onFailure {
      LOGGER.error("Display error", it)
    }
  }
}
