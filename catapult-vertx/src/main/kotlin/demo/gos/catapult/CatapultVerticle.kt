package demo.gos.catapult

import demo.gos.common.Areas
import demo.gos.common.Commons
import demo.gos.common.Commons.kafkaConfigProducer
import demo.gos.common.Players
import demo.gos.common.Noise
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import io.vertx.kotlin.core.executeBlockingAwait
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.*
import kotlin.math.atan
import kotlin.math.tan

val LOGGER: Logger = LoggerFactory.getLogger("Catapult-Vertx")
const val PORT = 8889
const val DELTA_MS: Long = 200
val LOAD_FACTOR = Commons.getDoubleEnv("LOAD_FACTOR", 0.1)
//// Accuracy [0, 1]
val ACCURACY = Commons.getDoubleEnv("ACCURACY", 0.9)
val SHOT_RANGE = Commons.getDoubleEnv("SHOT_RANGE", 400.0)
//val DETERMINIST = Commons.getStringEnv("DETERMINIST", "false") == "true"
val SPEED = Commons.getDoubleEnv("SPEED", 110.0)
val IMPACT_ZONE = Commons.getDoubleEnv("IMPACT_ZONE", 75.0)

val RND = SecureRandom()

class CatapultVerticle : CoroutineVerticle() {
  private lateinit var cata: Catapult

  override suspend fun start() {
    cata = initCatapult()
  }

  private suspend fun initCatapult(): Catapult {
    val id = "CATA-VX-" + UUID.randomUUID().toString()

    val area = Areas.spawnWeaponArea
    val pos = Players.spawnIn(area)
    val cata = Catapult(vertx, id, pos.x(), pos.y())

    val router = Router.router(vertx)
    router.get("/load").handler { GlobalScope.launch(vertx.dispatcher()) { cata.load(it) } }

    vertx.createHttpServer().requestHandler(router).listenAwait(PORT)
    LOGGER.info("HTTPS server started on port $PORT")

    return cata
  }

}

class Catapult(private val vertx: Vertx, private val id: String, private val x: Double, private val y: Double) {
  private var boulders = mutableListOf<Boulder>()
  private var gauge = 0.0
  private var inError = false
  private val kafkaProducer = KafkaProducer.create<String, JsonObject>(vertx, kafkaConfigProducer)

  init {

    vertx.setPeriodic(DELTA_MS) {
      GlobalScope.launch(vertx.dispatcher()) {
        update(DELTA_MS.toDouble() / 1000.0)
      }
    }
  }

  private suspend fun update(delta: Double) {
    makeNoise()
    display()
    val newBoulders = boulders.filter { !it.update(delta) }
    boulders = newBoulders.toMutableList()
  }

  private fun makeNoise() {
    kafkaProducer.write(KafkaProducerRecord.create("weapon-making-noise", JsonObject.mapFrom(Noise(id, x, y)))) { ar ->
      if (!ar.succeeded()) {
        LOGGER.error("Noise error", ar.cause())
      }
    }
  }

  suspend fun load(ctx: RoutingContext) {
    // Do something CPU intensive
    val d = vertx.executeBlockingAwait<Double> {
      val d = ctx.request().getParam("val")?.toDoubleOrNull() ?: 0.0
      it.complete(tan(atan(tan(atan(d)))))
    }
    ctx.response().end(d.toString())
    gauge += LOAD_FACTOR
    if (gauge >= 1.0) {
      // Shoot!
      shoot()
      gauge = 0.0
    }
  }


  private suspend fun shoot() {
    // TODO Recode this with new events
    /*
   val pos = Point(x, y)
   kotlin.runCatching {
     // Get all villains
     val res = WebClient.create(vertx).get(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/gm/elements")
       .addQueryParam("type", ElementType.VILLAIN.toString())
       .addQueryParam("status", ElementStatus.ALIVE.toString()).sendAwait()

     // Filter within range
     val all = res.bodyAsJsonArray().mapNotNull { if (it is JsonObject) it else null }
     val inRange = all.filter {
       val p = Point(it.getDouble("x"), it.getDouble("y"))
       pos.diff(p).size() <= SHOT_RANGE
     }
     // Accuracy modifier
     var angle = RND.nextDouble() * (1.0 - ACCURACY) * Math.PI
     if (RND.nextInt(2) == 0) {
       angle *= -1
     }
     val target = when {
       inRange.isNotEmpty() -> {
         val elt = inRange.random()
         val seg = Segment(pos, Point(elt.getDouble("x"), elt.getDouble("y")))
         seg.derivate().normalize().rotate(angle).mult(seg.size()).add(pos)
       }
       all.isNotEmpty() -> {
         val elt = all.random()
         val eltPos = Point(elt.getDouble("x"), elt.getDouble("y"))
         Segment(pos, eltPos).derivate().normalize().rotate(angle).mult(SHOT_RANGE).add(pos)
       }
       else -> null
     }

     if (target != null) {
       // Accuracy modifier
       val boulder = Boulder(vertx, id, pos, target, SPEED, IMPACT_ZONE)
       boulders.add(boulder)
     }
   }.onFailure {
     LOGGER.error("Shoot error", it)
   }

     */
  }

  private fun display() {
    val red = (gauge * 255).toInt()
    val color = if (inError) "red" else "rgb($red,128,128)"
    val json = JsonObject()
      .put("id", id)
      .put("style", "position: absolute; background-color: $color; height: 50px; width: 50px; z-index: 7;")
      .put("text", "")
      .put("x", x - 25)
      .put("y", y - 25)

    kafkaProducer.write(KafkaProducerRecord.create("display", json)) { ar ->
      if (!ar.succeeded()) {
        LOGGER.error("Display error", ar.cause())
      }
    }
  }


}
