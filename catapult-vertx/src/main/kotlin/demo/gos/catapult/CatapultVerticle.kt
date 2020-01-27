package demo.gos.catapult

import demo.gos.common.Commons
import demo.gos.common.DisplayData
import demo.gos.common.GameCommand
import demo.gos.common.Noise
import demo.gos.common.maths.Point
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.kafka.client.consumer.KafkaConsumer
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import io.vertx.kotlin.core.executeBlockingAwait
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.kafka.client.producer.writeAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

val LOGGER: Logger = LoggerFactory.getLogger("Catapult-Vertx")
const val PORT = 8889
const val DELTA_MS: Long = 1000
val colorize = fun(gauge: Double): String {
  val red = (gauge * 255).toInt()
  return "rgb($red,128,128)"
}

class CatapultVerticle : CoroutineVerticle() {
  private lateinit var cata: Catapult

  override suspend fun start() {
    val id = "CATA-VX-" + UUID.randomUUID().toString()
    cata = Catapult(vertx, id)

    val router = Router.router(vertx)
    router.get("/load").handler { GlobalScope.launch(vertx.dispatcher()) { cata.load(it.request().getParam("val")?.toDoubleOrNull()) } }

    vertx.createHttpServer().requestHandler(router).listenAwait(PORT)
    LOGGER.info("HTTPS server started on port $PORT")
  }
}

class Catapult(private val vertx: Vertx, id: String)
    : BaseCatapult(id, colorize) {
  private val kafkaProducer = KafkaProducer.create<String, JsonObject>(vertx, Commons.kafkaConfigProducer)

  init {
    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer(id))
      .subscribe("game").handler { onGameCommand(it.value().mapTo(GameCommand::class.java)) }

    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer(id))
      .subscribe("villain-making-noise").handler { listenToVillains(it.value().mapTo(Noise::class.java)) }

    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer(id))
      .subscribe("load-catapult").handler { record ->
        val value = record.value()
        if (value.getString("id") == id) {
          GlobalScope.launch(vertx.dispatcher()) {
            load(value.getDouble("val"))
          }
        }
    }

    vertx.setPeriodic(DELTA_MS) {
      GlobalScope.launch(vertx.dispatcher()) {
        update(DELTA_MS.toDouble() / 1000.0)
      }
    }
  }

  suspend fun load(value: Double?): Double? {
    val res = super.load { loadBlocking ->
      vertx.executeBlockingAwait {
        it.complete(loadBlocking(value))
      }
    }
    return res
  }

  override suspend fun makeNoise(noise: Noise) {
    kotlin.runCatching {
      kafkaProducer.writeAwait(KafkaProducerRecord.create("weapon-making-noise", JsonObject.mapFrom(noise)))
    }.onFailure {
      LOGGER.error("Noise error", it)
    }
  }

  override fun createBoulder(pos: Point, dest: Point, speed: Double, impact: Double): BaseBoulder {
    return Boulder(vertx, pos, dest, speed, impact)
  }

  override suspend fun display(data: DisplayData) {
    kotlin.runCatching {
      kafkaProducer.writeAwait(KafkaProducerRecord.create("display", JsonObject.mapFrom(data)))
    }.onFailure {
      LOGGER.error("Display error", it)
    }
  }
}
