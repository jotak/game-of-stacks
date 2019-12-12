package demo.gos.villains

import demo.gos.common.Areas
import demo.gos.common.Commons
import demo.gos.common.Point
import demo.gos.common.Segment
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.sendAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.*
import kotlin.math.min

val LOGGER: Logger = LoggerFactory.getLogger("Villains")
const val PORT = 8888
const val DELTA_MS: Long = 300
val WAVES_SIZE = Commons.getIntEnv("WAVES_SIZE", 10)
val WAVES_DELAY = Commons.getDoubleEnv("WAVES_DELAY", 10.0)
val SPEED = Commons.getDoubleEnv("SPEED", 35.0)
// Accuracy [0, 1]
val ACCURACY = Commons.getDoubleEnv("ACCURACY", 0.7)
val SHOT_RANGE = Commons.getDoubleEnv("SHOT_RANGE", 20.0)
val MIN_SPEED = ACCURACY * SPEED
const val TYPE_VILLAIN = "VILLAIN"
const val TYPE_HERO = "HERO"
val RND = SecureRandom()

enum class State {
  DEAD, ALIVE
}

data class Target(val id: String, var pos: Point, var status: State) {
  companion object {
    fun fromJson(json: JsonObject): Target {
      return Target(json.getString("id"), Point(json.getDouble("x"), json.getDouble("y")), State.valueOf(json.getString("status")))
    }
  }
}

data class Area(val x: Double, val y: Double, val width: Double, val height: Double) {
  companion object {
    fun fromJson(json: JsonObject): Area {
      return Area(json.getDouble("x"), json.getDouble("y"), json.getDouble("width"), json.getDouble("height"))
    }
  }
  fun spawn(): Point {
    return Point(x + RND.nextDouble() * width, y + RND.nextDouble() * height)
  }
}

fun moveActionAsJson(id: String, pos: Point): JsonObject {
  return JsonObject().put("id", id).put("x", pos.x()).put("y", pos.y())
}

fun kamikazeKillActionAsJson(killerId: String, targetId: String): JsonObject {
  return JsonObject().put("killerId", killerId).put("targetId", targetId).put("kamikaze", true)
}

class VillainsVerticle : CoroutineVerticle() {
  private lateinit var villains: Villains

  override suspend fun start() {
    villains = initVillains()
  }

  private fun initVillains(): Villains {
//    val router = Router.router(vertx)
//      router.get("/load").handler { GlobalScope.launch(vertx.dispatcher()) { cata.load(it) } }

//    vertx.createHttpServer().requestHandler(router).listenAwait(PORT)
//    LOGGER.info("HTTPS server started on port $PORT")

    return Villains(vertx)
  }
}

class Villains(private val vertx: Vertx) {
  private var waveTimer = 0.0
  private val villains = mutableListOf<Villain>()

  init {
    vertx.eventBus().consumer<Any>("start") {
      LOGGER.info("Received start event")
    }

    vertx.eventBus().consumer<Any>("stop") {
      LOGGER.info("Received stop event")
    }

    // Start game loop
    vertx.setPeriodic(DELTA_MS) {
      GlobalScope.launch(vertx.dispatcher()) {
        update(DELTA_MS.toDouble() / 1000.0)
      }
    }
  }

  private fun getArea(handler: Handler<Area>) {
    LOGGER.info("Pinging Game Manager & get spawn area...")
    WebClient.create(vertx).get(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/gm/areas/${Areas.SPAWN_VILLAINS}")
      .send { ar ->
        if (ar.succeeded()) {
          val code = ar.result().statusCode()
          if (code == 200) {
            LOGGER.info("Done")
            handler.handle(Area.fromJson(ar.result().bodyAsJsonObject()))
          } else {
            LOGGER.error("Failed: $code (skip wave)")
          }
        } else {
          LOGGER.error("Failed (skip wave)")
        }
      }
  }

  private fun createVillains(size: Int) {
    getArea(Handler { area->
      val newVillains = (0 until size).map {
        Villain("V-${UUID.randomUUID()}", area.spawn(), State.ALIVE, null)
      }
      villains.addAll(newVillains)
      WebClient.create(vertx).post(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/gm/element/batch").sendJson(
        JsonArray(newVillains.map { it.toJson() })
      ) { ar ->
        if (!ar.succeeded()) {
          LOGGER.error("Failed to create villains wave", ar.cause())
        }
      }
    })
  }

  private suspend fun update(delta: Double) {
    if (WAVES_SIZE > 0) {
      waveTimer -= delta
      if (waveTimer <= 0) {
        waveTimer = WAVES_DELAY
        createVillains(WAVES_SIZE)
      }
    }

    val alive = villains.filter { it.status != State.DEAD }
    val heroes = retrieveAllHeroes()
    alive.forEach { v ->
      val targetId = v.target?.id
      if (targetId != null) {
        v.target = heroes[targetId]
      } else if (heroes.isNotEmpty()) {
        v.target = heroes.values.random()
      }
      val dest = v.target?.pos ?: Point(1000.0, 1000.0)
      walkToDestination(delta, v, dest)
    }
    updateStates(alive)

    villains.forEach { display(it) }
  }

  private suspend fun retrieveAllHeroes(): Map<String, Target> {
    val res = WebClient.create(vertx).get(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/gm/elements")
        .addQueryParam("type", TYPE_HERO)
        .addQueryParam("status", State.ALIVE.toString()).sendAwait()

    val jsonArr = res.bodyAsJsonArray()
    return jsonArr.mapNotNull {
      if (it is JsonObject) {
        Target.fromJson(it)
      } else null
    }.associateBy({ it.id }, { it })
  }

  private fun updateStates(alive: List<Villain>) {
    val deadTargets = alive.filter { it.isOnTarget() }
      .map { kamikazeKillActionAsJson(it.id, it.target!!.id) }
    val patch = JsonArray(alive.map { moveActionAsJson(it.id, it.pos) })
    patch.addAll(JsonArray(deadTargets))
    WebClient.create(vertx).patch(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/gm/action/batch").sendJson(patch) { ar ->
      if (!ar.succeeded()) {
        ar.cause().printStackTrace()
      }
    }
  }

  private fun walkToDestination(delta: Double, v: Villain, dest: Point) {
    // Speed and angle are modified by accuracy
    val segToDest = Segment(v.pos, dest)
    // maxSpeed avoids stepping too high when close to destination
    val maxSpeed = min(segToDest.size() / delta, SPEED)
    // minSpeed must be kept <= maxSpeed
    val minSpeed = min(maxSpeed, MIN_SPEED)
    val speed = delta * (minSpeed + RND.nextDouble() * (maxSpeed - minSpeed))
    val relativeMove = randomishSegmentNormalized(segToDest).mult(speed)
    v.pos = v.pos.add(relativeMove)
  }

  private fun randomishSegmentNormalized(segToDest: Segment): Point {
    var angle = RND.nextDouble() * (1.0 - ACCURACY) * Math.PI
    if (RND.nextInt(2) == 0) {
      angle *= -1
    }
    return segToDest.derivate().normalize().rotate(angle)
  }

  private fun display(v: Villain) {
    val color = when (v.status) {
        State.ALIVE -> "#802020"
        State.DEAD -> "#101030"
    }
    val json = JsonObject()
      .put("id", v.id)
      .put("style", "position: absolute; background-color: $color; transition: top ${DELTA_MS}ms, left ${DELTA_MS}ms; height: 30px; width: 30px; border-radius: 50%; z-index: 8;")
      .put("text", "")
      .put("x", v.pos.x() - 15)
      .put("y", v.pos.y() - 15)

    WebClient.create(vertx).post(Commons.UI_PORT, Commons.UI_HOST, "/display").sendJson(json) { ar ->
      if (!ar.succeeded()) {
        ar.cause().printStackTrace()
      }
    }
  }
}
