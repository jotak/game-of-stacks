package demo.gos.gos_villains

import demo.gos.common.Commons
import demo.gos.common.Rectangle
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router

class MockedBattlefield : AbstractVerticle() {
  private var battlefieldInfo: BattlefieldInfo = BattlefieldInfo(Rectangle(0.0, 0.0, 500.0, 500.0))
  private val nedStark = JsonObject().put("id", "Ned Stark").put("x", 500.0).put("y", 400.0)

  override fun start(startFuture: Future<Void>) {
    val serverOptions = HttpServerOptions().setPort(Commons.BATTLEFIELD_PORT)
    val router = Router.router(vertx)
    router.get("/villains/area").handler {
      val json = JsonObject(battlefieldInfo.spawnArea.toMap())
      it.response().end(json.toString())
    }
    router.get("/pick/:count/heroes").handler {
      val arr = arrayOfNulls<JsonObject>(it.request().getParam("count").toInt()).map { nedStark }
      it.response().end(JsonArray(arr).toString())
    }
    router.get("/elements").handler {
      val ids = it.request().getParam("ids").split(',')
      val arr = arrayOfNulls<JsonObject>(ids.size).map { nedStark }
      it.response().end(JsonArray(arr).toString())
    }

    vertx.createHttpServer().requestHandler(router).listen(serverOptions.port, serverOptions.host) { http ->
      if (http.succeeded()) {
        startFuture.complete()
        println("HTTP server started on port " + serverOptions.port)
      } else {
        startFuture.fail(http.cause())
      }
    }
  }
}
