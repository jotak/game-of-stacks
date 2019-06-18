package demo.gos.gos_villains

import demo.gos.common.Commons
import demo.gos.common.Point
import demo.gos.common.Rectangle
import demo.gos.common.Segment
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.SecureRandom
import kotlin.concurrent.thread
import kotlin.coroutines.resume

const val DELTA_MS: Long = 300
val CROWD_SIZE = Commons.getIntEnv("CROWD_SIZE", 20)
val WAVES_SIZE = Commons.getIntEnv("WAVES_SIZE", 5)
val WAVES_DELAY = Commons.getDoubleEnv("WAVES_DELAY", 10.0)
val SPAWN_CONTINUOUSLY = Commons.getStringEnv("SPAWN_CONTINUOUSLY", "false") == "true"
val DETERMINIST = Commons.getStringEnv("DETERMINIST", "false") == "true"
val SPEED = Commons.getDoubleEnv("SPEED", 60.0)
// Accuracy [0, 1]
val ACCURACY = Commons.getDoubleEnv("ACCURACY", 0.7)
val MIN_SPEED = ACCURACY * SPEED

enum class State {
  DEAD, ALIVE
}

data class Target(val id: String, var pos: Point)
data class Villain(val id: String, var pos: Point, var state: State, var target: Target?)
data class BattlefieldInfo(val spawnArea: Rectangle)

val DEFAULT_BATTLEFIELD = BattlefieldInfo(Rectangle(0.0, 0.0, 50.0, 50.0))

class Villains : AbstractVerticle() {
  private val villains = (1..CROWD_SIZE).map { Villain("VILLAIN-$it", Point.ZERO, State.ALIVE, null) }
  private lateinit var client: WebClient
  private lateinit var battlefieldInfo: BattlefieldInfo
  private val rnd = SecureRandom()

  override fun start(startFuture: Future<Void>) {
    client = WebClient.create(vertx)

    thread {
      runBlocking {
        battlefieldInfo = checkBattlefield()

        // Check regularly about battlefield dimensions
        vertx.setPeriodic(5000) {
          thread {
            runBlocking {
              battlefieldInfo = checkBattlefield()
              println("Battlefield dimensions: $battlefieldInfo")
            }
          }
        }

        // Start game loop
        vertx.setPeriodic(DELTA_MS) {
          thread {
            runBlocking {
              update(DELTA_MS.toDouble() / 1000.0)
            }
          }
        }
      }
    }

    vertx
      .createHttpServer()
      .requestHandler { req ->
        req.response()
          .putHeader("content-type", "text/plain")
          .end("List of villains: ${villains.joinToString(", ") { it.id }}")
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

  private suspend fun checkBattlefield(): BattlefieldInfo =
    suspendCancellableCoroutine { cont ->
      client.get(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/villains/area").send { ar ->
        if (!ar.succeeded()) {
          ar.cause().printStackTrace()
          cont.resume(DEFAULT_BATTLEFIELD)
        } else {
          val response = ar.result()
          val map = response.bodyAsJsonObject().map
          cont.resume(BattlefieldInfo(Rectangle.fromMap(map)))
        }
      }
    }

  private suspend fun update(delta: Double) {
    checkTargets()
    villains.forEach {v ->
      val dest = v.target?.pos ?: Point(1000.0, 1000.0)
      walkToDestination(delta, v, dest)
      display(v)
    }
  }

  private suspend fun checkTargets() {
    val ids = villains.mapNotNull { it.target?.id }
    val countMissing = villains.count { it.target == null }

    val updatedTargets = if (ids.isEmpty()) emptyMap() else updateTargets(ids)
    val newTargets = if (countMissing == 0) emptyList() else findTargets(countMissing)
    var missCounter = 0

    villains.forEach {
      val id = it.target?.id
      if (id != null) {
        it.target = updatedTargets[id]
      } else {
        val at = missCounter++
        if (at < newTargets.size) {
          it.target = newTargets[at]
        }
      }
    }
  }

  private suspend fun findTargets(count: Int): List<Target> =
    suspendCancellableCoroutine { cont ->
      client.get(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/pick/$count/heroes").send { ar ->
        if (!ar.succeeded()) {
          ar.cause().printStackTrace()
          cont.resume(emptyList())
        } else {
          val response = ar.result()
          val jsonArr = response.bodyAsJsonArray()
          val targets = jsonArr.mapNotNull {
            if (it is JsonObject) {
              Target(it.getString("id"), Point(it.getDouble("x"), it.getDouble("y")))
            } else null
          }
          cont.resume(targets)
        }
      }
    }

  private suspend fun updateTargets(ids: List<String>): Map<String, Target> =
    suspendCancellableCoroutine { cont ->
      client.get(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/elements")
            .addQueryParam("ids", ids.joinToString(",")).send { ar ->
        if (!ar.succeeded()) {
          ar.cause().printStackTrace()
          cont.resume(emptyMap())
        } else {
          val response = ar.result()
          val jsonArr = response.bodyAsJsonArray()
          val targets = jsonArr.mapNotNull {
            if (it is JsonObject) {
              Target(it.getString("id"), Point(it.getDouble("x"), it.getDouble("y")))
            } else null
          }.associateBy({it.id}, {it})
          cont.resume(targets)
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
    val json = JsonObject()
      .put("id", v.id)
      .put("style", "position: absolute; background-color: #101030; transition: top " + DELTA_MS + "ms, left " + DELTA_MS + "ms; height: 30px; width: 30px; border-radius: 50%; z-index: 8;")
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
