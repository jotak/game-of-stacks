package demo.gos.catapult

import demo.gos.common.Areas
import demo.gos.common.Commons
import demo.gos.common.Point
import io.vertx.core.*
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.executeBlockingAwait
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.*
import kotlin.math.atan
import kotlin.math.tan

const val PORT = 8889
const val DELTA_MS: Long = 300
//val DETERMINIST = Commons.getStringEnv("DETERMINIST", "false") == "true"
//val SPEED = Commons.getDoubleEnv("SPEED", 35.0)
//// Accuracy [0, 1]
//val ACCURACY = Commons.getDoubleEnv("ACCURACY", 0.7)
//val SHOT_RANGE = Commons.getDoubleEnv("SHOT_RANGE", 20.0)
//val MIN_SPEED = ACCURACY * SPEED

val rnd = SecureRandom()

data class Area(val x: Double, val y: Double, val width: Double, val height: Double) {
  companion object {
    fun fromJson(json: JsonObject): Area {
      return Area(json.getDouble("x"), json.getDouble("y"), json.getDouble("width"), json.getDouble("height"))
    }
  }
  fun spawn(): Point {
    return Point(x + rnd.nextDouble() * width, y + rnd.nextDouble() * height)
  }
}

open class CatapultVerticle : AbstractVerticle() {
  private lateinit var cata: Catapult

  override fun start(startFuture: Future<Void>) {
    GlobalScope.launch(vertx.dispatcher()) {
      cata = initCatapult(startFuture)
    }
  }

  private suspend fun initCatapult(startFuture: Future<Void>): Catapult {
    val id = "CATA-VX-" + UUID.randomUUID().toString()

    val area = awaitResult<Area> { h ->
      GlobalScope.launch(vertx.dispatcher()) {
        tryGetArea(id, Handler { js-> h.handle(js) })
      }
    }
    val pos = area.spawn()
    val cata = Catapult(vertx, id, pos.x(), pos.y())

    val router = Router.router(vertx)
    router.get("/load").handler { GlobalScope.launch(vertx.dispatcher()) { cata.load(it) } }

    vertx.createHttpServer().requestHandler(router).listen(PORT) { http ->
      if (http.succeeded()) {
        startFuture.complete()
        println("HTTPS server started on port $PORT")
      } else {
        startFuture.fail(http.cause())
      }
    }

    return cata
  }

  private suspend fun tryGetArea(id: String, handler: Handler<AsyncResult<Area>>) {
    println("Trying to init Catapult against Game Manager...")
    WebClient.create(vertx).get(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/gm/areas/${Areas.SPAWN_WEAPONS}")
        .send { ar ->
      if (ar.succeeded()) {
        val code = ar.result().statusCode()
        if (code == 200) {
          println("Done")
          handler.handle(ar.map { Area.fromJson(it.bodyAsJsonObject()) })
        } else {
          println("Failed: $code")
          vertx.setTimer(1000) {
            GlobalScope.launch(vertx.dispatcher()) {
              tryGetArea(id, handler)
            }
          }
        }
      } else {
        println("Failed")
        vertx.setTimer(1000) {
          GlobalScope.launch(vertx.dispatcher()) {
            tryGetArea(id, handler)
          }
        }
      }
    }
  }
}

class Catapult(private val vertx: Vertx, private val id: String, private val x: Double, private val y: Double) {
  private var color = "brown"

  init {
    WebClient.create(vertx).post(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/gm/element").sendJson(
      JsonObject().put("id", id).put("type", "CATAPULT").put("x", x).put("y", y)
    ) { ar ->
      if (ar.succeeded()) {
        println("Catapult registered on Game Manager")
      } else {
        println("Failed to register catapult on Game Manager")
        ar.cause()?.printStackTrace()
        color = "red"
      }
    }

    vertx.setPeriodic(DELTA_MS) {
      GlobalScope.launch(vertx.dispatcher()) {
        update(DELTA_MS.toDouble() / 1000.0)
      }
    }
  }

  private suspend fun update(delta: Double) {
    println("Updating catapult")
    display()
  }

  suspend fun load(ctx: RoutingContext) {
    // Do something CPU intensive
    val d = vertx.executeBlockingAwait<Double> {
      val d = ctx.request().getParam("val")?.toDoubleOrNull() ?: 0.0
      it.complete(tan(atan(tan(atan(d)))))
    }
    ctx.response().end(d.toString())
  }

  private fun display() {
    val json = JsonObject()
      .put("id", id)
      .put("style", "position: absolute; background-color: $color; height: 50px; width: 50px; z-index: 7;")
      .put("text", "")
      .put("x", x - 25)
      .put("y", y - 25)

    val client = WebClient.create(vertx)
    client.post(Commons.UI_PORT, Commons.UI_HOST, "/display").sendJson(json) { ar ->
      if (!ar.succeeded()) {
        ar.cause().printStackTrace()
      }
    }
  }
}
