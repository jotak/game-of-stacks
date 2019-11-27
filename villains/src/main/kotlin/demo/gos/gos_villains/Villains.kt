package demo.gos.gos_villains

import demo.gos.common.Commons
import demo.gos.common.Point
import demo.gos.common.Segment
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.SecureRandom
import java.util.*
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.math.abs

const val DELTA_MS: Long = 300
val CROWD_SIZE = Commons.getIntEnv("CROWD_SIZE", 1)
val WAVES_SIZE = Commons.getIntEnv("WAVES_SIZE", 1)
val WAVES_DELAY = Commons.getDoubleEnv("WAVES_DELAY", 5.0)
val DETERMINIST = Commons.getStringEnv("DETERMINIST", "false") == "true"
val SPEED = Commons.getDoubleEnv("SPEED", 35.0)
// Accuracy [0, 1]
val ACCURACY = Commons.getDoubleEnv("ACCURACY", 0.7)
val SHOT_RANGE = Commons.getDoubleEnv("SHOT_RANGE", 20.0)
val MIN_SPEED = ACCURACY * SPEED
const val TYPE_VILLAIN = "VILLAIN"
const val TYPE_HERO = "HERO"

enum class State {
  DEAD, ALIVE
}

data class Target(val id: String, var pos: Point, var status: State) {
  fun toJson(): JsonObject {
    return JsonObject().put("id", id).put("x", pos.x()).put("y", pos.y()).put("type", TYPE_HERO).put("status", status)
  }
}

fun targetFromJson(json: JsonObject): Target {
  return Target(json.getString("id"), Point(json.getDouble("x"), json.getDouble("y")), State.valueOf(json.getString("status")))
}

data class Villain(val id: String, var pos: Point, var status: State, var target: Target?) {
  fun toJson(): JsonObject {
    return JsonObject().put("id", id).put("x", pos.x()).put("y", pos.y()).put("type", TYPE_VILLAIN).put("status", status)
  }

  fun isOnTarget(): Boolean {
    val diff = target?.pos?.diff(pos)
    // use a shot range
    return diff != null && diff.size() <= SHOT_RANGE
  }
}

fun moveActionAsJson(id: String, pos: Point): JsonObject {
  return JsonObject().put("id", id).put("x", pos.x()).put("y", pos.y())
}

fun kamikazeKillActionAsJson(killerId: String, targetId: String): JsonObject {
  return JsonObject().put("killerId", killerId).put("targetId", targetId).put("kamikaze", true)
}

fun villainFromJson(json: JsonObject): Villain {
  return Villain(json.getString("id"), Point(json.getDouble("x"), json.getDouble("y")), State.valueOf(json.getString("status")), null)
}

class Villains : AbstractVerticle() {
  // private var villains = mutableListOf<Villain>()
  private lateinit var client: WebClient
  private val rnd = SecureRandom()
  private var running = false
  private var waveTimer = WAVES_DELAY

  override fun start(startFuture: Future<Void>) {
    client = WebClient.create(vertx)

    thread {
      runBlocking {
        // Start game loop
        vertx.setPeriodic(DELTA_MS) {
          thread {
            runBlocking {
              if (running) {
                update(DELTA_MS.toDouble() / 1000.0)
              }
            }
          }
        }
      }
    }

    vertx
      .createHttpServer()
      .requestHandler { req ->
        when {
          req.path() == "/start" -> {
            createVillains(CROWD_SIZE)
            running = true
          }
          req.path() == "/stop" -> running = false
          else -> {
            println("Received request on path ${req.path()}")
            req.response()
              .putHeader("content-type", "text/plain")
              .end("")
          }
        }
      }
      .listen(8888) { http ->
        if (http.succeeded()) {
          startFuture.complete()
          println("HTTP server started on port 8888")
        } else {
          startFuture.fail(http.cause())
        }
      }
  }

  private fun createVillains(size: Int) {
    val spaceAroundEach = 60
    val newVillains = (0 until size).map {
      var x = (-100 - spaceAroundEach * (it / 10)).toDouble()
      var y = (spaceAroundEach * (it % 10)).toDouble()
      if (!DETERMINIST) {
        x += spaceAroundEach * 2 * (rnd.nextDouble() - 0.5)
        y += spaceAroundEach * 2 * (rnd.nextDouble() - 0.5)
      }
      Villain("V-${UUID.randomUUID()}", Point(x, y), State.ALIVE, null)
    }

    client.post(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/gm/element/batch").sendJson(
      JsonArray(newVillains.map { it.toJson() })
    ) { ar ->
      if (!ar.succeeded()) {
        ar.cause().printStackTrace()
      }
    }
  }

  private suspend fun update(delta: Double) {
    if (WAVES_SIZE > 0) {
      waveTimer -= delta
      if (waveTimer <= 0) {
        waveTimer = WAVES_DELAY
        createVillains(WAVES_SIZE)
      }
    }

    val all = retrieveAllVillains()
    val alive = all.filter { it.status != State.DEAD }
    checkTargets(alive)
    alive.forEach { v ->
      val dest = v.target?.pos ?: Point(1000.0, 1000.0)
      walkToDestination(delta, v, dest)
    }
    updateStates(alive)

    all.forEach { display(it) }
  }

  private suspend fun retrieveAllVillains(): List<Villain> =
    suspendCancellableCoroutine { cont ->
      client.get(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/gm/elements")
        .addQueryParam("type", TYPE_VILLAIN)
        .send { ar ->
          if (!ar.succeeded()) {
            ar.cause().printStackTrace()
            cont.resume(emptyList())
          } else {
            val response = ar.result()
            val jsonArr = response.bodyAsJsonArray()
            val list = jsonArr.mapNotNull {
              if (it is JsonObject) {
                villainFromJson(it)
              } else null
            }
            cont.resume(list)
          }
        }
    }

  private suspend fun checkTargets(alive: List<Villain>) {
    val targets = retrieveAllTargets()
    alive.forEach {
      val id = it.target?.id
      if (id != null) {
        it.target = targets[id]
      } else if (targets.isNotEmpty()) {
        it.target = targets.values.random()
      }
    }
  }

  private suspend fun retrieveAllTargets(): Map<String, Target> =
    suspendCancellableCoroutine { cont ->
      client.get(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/gm/elements")
        .addQueryParam("type", TYPE_HERO)
        .addQueryParam("status", State.ALIVE.toString()).send { ar ->
          if (!ar.succeeded()) {
            ar.cause().printStackTrace()
            cont.resume(emptyMap())
          } else {
            val response = ar.result()
            val jsonArr = response.bodyAsJsonArray()
            val list = jsonArr.mapNotNull {
              if (it is JsonObject) {
                targetFromJson(it)
              } else null
            }.associateBy({ it.id }, { it })
            cont.resume(list)
          }
        }
    }

  private fun updateStates(alive: List<Villain>) {
    val deadTargets = alive.filter { it.isOnTarget() }
      .map { kamikazeKillActionAsJson(it.id, it.target!!.id) }
    val patch = JsonArray(alive.map { moveActionAsJson(it.id, it.pos) })
    patch.addAll(JsonArray(deadTargets))
    client.patch(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/gm/action/batch").sendJson(patch) { ar ->
      if (!ar.succeeded()) {
        ar.cause().printStackTrace()
      }
    }
  }

  private fun walkToDestination(delta: Double, v: Villain, dest: Point) {
    // Speed and angle are modified by accuracy
    val segToDest = Segment(v.pos, dest)
    // maxSpeed avoids stepping too high when close to destination
    val maxSpeed = Math.min(segToDest.size() / delta, SPEED)
    // minSpeed must be kept <= maxSpeed
    val minSpeed = Math.min(maxSpeed, MIN_SPEED)
    val speed = if (DETERMINIST) {
      delta * (minSpeed + (maxSpeed - minSpeed) / 2)
    } else {
      delta * (minSpeed + rnd.nextDouble() * (maxSpeed - minSpeed))
    }
    val relativeMove = if (DETERMINIST) {
      segToDest.derivate().normalize().mult(speed)
    } else {
      randomishSegmentNormalized(segToDest).mult(speed)
    }
    v.pos = v.pos.add(relativeMove)
  }

  private fun randomishSegmentNormalized(segToDest: Segment): Point {
    var angle = rnd.nextDouble() * (1.0 - ACCURACY) * Math.PI
    if (rnd.nextInt(2) == 0) {
      angle *= -1
    }
    return segToDest.derivate().normalize().rotate(angle)
  }

  private fun display(v: Villain) {
    val color = when {
      v.status == State.ALIVE -> "#802020"
      v.status == State.DEAD -> "#101030"
      else -> "#505060"
    }
    val json = JsonObject()
      .put("id", v.id)
      .put("style", "position: absolute; background-color: $color; transition: top ${DELTA_MS}ms, left ${DELTA_MS}ms; height: 30px; width: 30px; border-radius: 50%; z-index: 8;")
      .put("text", "")
      .put("x", v.pos.x() - 15)
      .put("y", v.pos.y() - 15)

    client.post(Commons.UI_PORT, Commons.UI_HOST, "/display").sendJson(json) { ar ->
      if (!ar.succeeded()) {
        ar.cause().printStackTrace()
      }
    }
  }
}
