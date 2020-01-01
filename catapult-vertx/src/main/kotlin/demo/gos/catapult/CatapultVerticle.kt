package demo.gos.catapult

import demo.gos.common.Areas
import demo.gos.common.Commons
import demo.gos.common.Noise
import demo.gos.common.maths.Point
import demo.gos.common.maths.Segment
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
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
import java.security.SecureRandom
import java.util.*
import kotlin.math.atan
import kotlin.math.min
import kotlin.math.tan

val LOGGER: Logger = LoggerFactory.getLogger("Catapult-Vertx")
const val PORT = 8889
const val DELTA_MS: Long = 200
val LOAD_FACTOR = Commons.getDoubleEnv("LOAD_FACTOR", 0.1)
// Accuracy [0, 1]
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
    val pos = Areas.spawnWeaponArea.spawn(RND)
    val cata = Catapult(vertx, id, pos)

    val router = Router.router(vertx)
    router.get("/load").handler { GlobalScope.launch(vertx.dispatcher()) { cata.load(it) } }

    vertx.createHttpServer().requestHandler(router).listenAwait(PORT)
    LOGGER.info("HTTPS server started on port $PORT")

    return cata
  }
}

class Catapult(private val vertx: Vertx, private val id: String, private val pos: Point) {
  private val kafkaProducer = KafkaProducer.create<String, JsonObject>(vertx, Commons.kafkaConfigProducer)
  private var boulders = mutableListOf<Boulder>()
  private var target: Noise? = null
  private var gauge = 0.0
  private var isPaused = false

  init {
    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer(id))
      .subscribe("game").handler { onGameControls(it.value()) }

    KafkaConsumer.create<String, JsonObject>(vertx, Commons.kafkaConfigConsumer(id))
      .subscribe("villain-making-noise").handler { listenToVillains(it.value()) }

    vertx.setPeriodic(DELTA_MS) {
      GlobalScope.launch(vertx.dispatcher()) {
        update(DELTA_MS.toDouble() / 1000.0)
      }
    }
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
    gauge = 0.0
    target = null
    boulders = mutableListOf()
  }

  private suspend fun update(delta: Double) {
    if (!isPaused) {
      makeNoise()
      val newBoulders = boulders.filter { !it.update(delta) }
      boulders = newBoulders.toMutableList()
    }
    display()
  }

  private suspend fun makeNoise() {
    kotlin.runCatching {
      kafkaProducer.writeAwait(KafkaProducerRecord.create("weapon-making-noise", JsonObject.mapFrom(Noise.fromPoint(id, pos))))
    }.onFailure {
      LOGGER.error("Noise error", it)
    }
  }

  suspend fun load(ctx: RoutingContext) {
    if (isPaused) {
      ctx.response().end("no (I'm paused)")
      return
    }
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
      target = null
    }
  }

  // listen to noise; when target seems more interesting than the current one, take it instead
  private fun listenToVillains(json: JsonObject) {
    val noise = json.mapTo(Noise::class.java)
    val noisePos = noise.toPoint()
    val currentTarget = target
    if (currentTarget == null) {
      LOGGER.info("Catapult has elected a target at $noisePos")
      target = noise
    } else {
      if (noise.id == currentTarget.id) {
        // Update target position
        target = noise
      } else {
        val currentDist = Segment(pos, currentTarget.toPoint()).size()
        val newDist = Segment(pos, noisePos).size()
        // 5% chances to get attention
        if (newDist < currentDist && RND.nextInt(100) < 5) {
          LOGGER.info("Catapult has elected a different target at $noisePos")
          target = noise
        }
      }
    }
  }

  private fun shoot() {
    val t = target
    if (t != null) {
      // Accuracy modifier
      var angle = RND.nextDouble() * (1.0 - ACCURACY) * Math.PI
      if (RND.nextInt(2) == 0) {
        angle *= -1
      }
      // Is in range?
      val segToDest = Segment(pos, t.toPoint())
      val shootSize = min(segToDest.size(), SHOT_RANGE)
      val dest = segToDest.derivate().normalize().rotate(angle).mult(shootSize).add(pos)
      val boulder = Boulder(vertx, pos, dest, SPEED, IMPACT_ZONE)
      boulders.add(boulder)
    }
  }

  private suspend fun display() {
    val red = (gauge * 255).toInt()
    val color = "rgb($red,128,128)"
    val json = JsonObject()
      .put("id", id)
      .put("style", "position: absolute; background-color: $color; height: 50px; width: 50px; z-index: 7;")
      .put("text", "")
      .put("x", pos.x() - 25)
      .put("y", pos.y() - 25)

    kotlin.runCatching {
      kafkaProducer.writeAwait(KafkaProducerRecord.create("display", json))
    }.onFailure {
      LOGGER.error("Display error", it)
    }
  }
}
