package demo.gos.gos_villains

import demo.gos.common.Commons
import demo.gos.common.Point
import demo.gos.common.Rectangle
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.ext.web.client.WebClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

const val DELTA_MS: Long = 300
val CROWD_SIZE = Commons.getIntEnv("CROWD_SIZE", 20)
val WAVES_SIZE = Commons.getIntEnv("WAVES_SIZE", 5)
val WAVES_DELAY = Commons.getDoubleEnv("WAVES_DELAY", 10.0)
val SPAWN_CONTINUOUSLY = Commons.getStringEnv("SPAWN_CONTINUOUSLY", "false") == "true"
val DETERMINIST = Commons.getStringEnv("DETERMINIST", "true") == "true"

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
  lateinit var battlefieldInfo: BattlefieldInfo

  override fun start(startFuture: Future<Void>) {
    client = WebClient.create(vertx)

    runBlocking {
      battlefieldInfo = checkBattlefield()

      // Check regularly about battlefield dimensions
      vertx.setPeriodic(5000) {
        runBlocking {
          battlefieldInfo = checkBattlefield()
        }
      }

      // Start game loop
      vertx.setPeriodic(DELTA_MS) {
        runBlocking {
          update(DELTA_MS.toDouble() / 1000.0)
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
    villains.forEach {
      it.target = checkTarget(it)

    }
  }

  private suspend fun checkTarget(villain: Villain): Target? {
    val id = villain.target?.id
    return if (id == null) {
      findTarget()
    } else {
      updateTarget(id)
    }
  }

  private suspend fun findTarget(): Target? =
    suspendCancellableCoroutine { cont ->
      client.get(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/heroes/target").send { ar ->
        if (!ar.succeeded()) {
          ar.cause().printStackTrace()
          cont.resume(null)
        } else {
          val response = ar.result()
          val json = response.bodyAsJsonObject()
          val id = json.getString("id")
          val x = json.getDouble("x")
          val y = json.getDouble("y")
          if (id != null && x != null && y != null) {
            cont.resume(Target(id, Point(x, y)))
          } else {
            cont.resume(null)
          }
        }
      }
    }

  private suspend fun updateTarget(id: String): Target? =
    suspendCancellableCoroutine { cont ->
      client.get(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/heroes/target/$id").send { ar ->
        if (!ar.succeeded()) {
          ar.cause().printStackTrace()
          cont.resume(null)
        } else {
          val response = ar.result()
          val json = response.bodyAsJsonObject()
          val x = json.getDouble("x")
          val y = json.getDouble("y")
          if (x != null && y != null) {
            cont.resume(Target(id, Point(x, y)))
          } else {
            cont.resume(null)
          }
        }
      }
    }

//  private suspend fun walkRandom(delta: Double, villain: Villain) {
//    if (currentDestination == null || new Segment(pos, currentDestination).size() < 10) {
//      currentDestination = randomDestination();
//    }
//    walkToDestination(spanContext, delta);
//  }
//
//  private suspend fun walkToDestination(delta: Double, villain: Villain) {
//    if (currentDestination != null) {
//      // Speed and angle are modified by accuracy
//      Segment segToDest = new Segment(pos, currentDestination);
//      // maxSpeed avoids stepping to high when close to destination
//      double maxSpeed = Math.min(segToDest.size() / delta, SPEED);
//      // minSpeed must be kept <= maxSpeed
//      double minSpeed = Math.min(maxSpeed, MIN_SPEED);
//      double speed = delta * (minSpeed + rnd.nextDouble() * (maxSpeed - minSpeed));
//      Point relativeMove = randomishSegmentNormalized(segToDest).mult(speed);
//      pos = pos.add(relativeMove);
//      display(spanContext);
//    }
//  }
}
