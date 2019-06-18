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
import kotlin.concurrent.thread
import kotlin.coroutines.resume

const val DELTA_MS: Long = 300
val CROWD_SIZE = Commons.getIntEnv("CROWD_SIZE", 20)
val WAVES_SIZE = Commons.getIntEnv("WAVES_SIZE", 10)
val WAVES_DELAY = Commons.getDoubleEnv("WAVES_DELAY", 6.0)
val DETERMINIST = Commons.getStringEnv("DETERMINIST", "false") == "true"
val SPEED = Commons.getDoubleEnv("SPEED", 35.0)
// Accuracy [0, 1]
val ACCURACY = Commons.getDoubleEnv("ACCURACY", 0.7)
val MIN_SPEED = ACCURACY * SPEED

enum class State {
  DEAD, ALIVE, UNSEEN
}

data class Target(val id: String, var pos: Point)
data class Villain(val id: String, var pos: Point, var state: State, var target: Target?)

class Villains : AbstractVerticle() {
  private var villains = mutableListOf<Villain>()
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
            initVillains()
            running = true
          }
          req.path() == "/stop" -> running = false
          else -> {
            println("Received request on path ${req.path()}")
            req.response()
              .putHeader("content-type", "text/plain")
              .end("List of villains: ${villains.joinToString(", ") { it.id }}")
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

  private fun allAlive() = villains.filter { it.state != State.DEAD }

  private fun initVillains() {
    villains.clear()
    createVillains(CROWD_SIZE)
  }

  private fun createVillains(size: Int) {
    val spaceAroundEach = 60
    val idOffset = villains.size
    val newVillains = (0 until size).map {
      var x = (-100 - spaceAroundEach * (it / 10)).toDouble()
      var y = (spaceAroundEach * (it % 10)).toDouble()
      if (!DETERMINIST) {
        x += spaceAroundEach * 2 * (rnd.nextDouble() - 0.5)
        y += spaceAroundEach * 2 * (rnd.nextDouble() - 0.5)
      }
      Villain("VILLAIN-${it + idOffset}", Point(x, y), State.ALIVE, null)
    }
    villains.addAll(newVillains)
  }

  private suspend fun update(delta: Double) {
    if (WAVES_SIZE > 0) {
      waveTimer -= delta
      if (waveTimer <= 0) {
        waveTimer = WAVES_DELAY
        createVillains(WAVES_SIZE)
      }
    }

    checkTargets()
    allAlive().forEach {v ->
      val dest = v.target?.pos ?: Point(1000.0, 1000.0)
      walkToDestination(delta, v, dest)
    }
    updateStates()

    villains.forEach { display(it) }
  }

  private suspend fun checkTargets() {
    val ids = allAlive().mapNotNull { it.target?.id }
    val countMissing = allAlive().count { it.target == null }

    val updatedTargets = if (ids.isEmpty()) emptyMap() else updateTargets(ids)
    val newTargets = if (countMissing == 0) emptyList() else findTargets(countMissing)
    var missCounter = 0

    allAlive().forEach {
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

  private fun updateStates() {
    val toUpdate = allAlive()
    client.post(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/elements").sendJson(
      JsonArray(toUpdate.map {
        JsonObject().put("id", it.id).put("x", it.pos.x()).put("y", it.pos.y()).put("type", "VILLAIN")
      })
    ) { ar ->
      if (!ar.succeeded()) {
        ar.cause().printStackTrace()
        toUpdate.forEach { it.state = State.UNSEEN }
      } else if (ar.result().statusCode() != 200) {
        toUpdate.forEach { it.state = State.UNSEEN }
      } else {
        val response = ar.result()
        val json = response.bodyAsJsonObject()
        toUpdate.forEach {
          it.state = when {
            json.getString(it.id) == "DEAD" -> State.DEAD
            json.getString(it.id) == "ALIVE" -> State.ALIVE
            else -> State.UNSEEN
          }
        }
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
      v.state == State.ALIVE -> "#802020"
      v.state == State.DEAD -> "#101030"
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
