package demo.gos.gos_villains

import demo.gos.common.Commons
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router

class MockedBattlefield : AbstractVerticle() {
  private val nedStark = JsonObject().put("id", "Ned Stark").put("x", 500.0).put("y", 400.0)

  override fun start(startFuture: Future<Void>) {
    val serverOptions = HttpServerOptions().setPort(Commons.BATTLEFIELD_PORT)
    val router = Router.router(vertx)
    router.get("/pick/:count/heroes").handler {
      val arr = arrayOfNulls<JsonObject>(it.request().getParam("count").toInt()).map { nedStark }
      it.response().end(JsonArray(arr).toString())
    }
    router.get("/elements").handler {
      val ids = it.request().getParam("ids").split(',')
      val arr = arrayOfNulls<JsonObject>(ids.size).map { nedStark }
      it.response().end(JsonArray(arr).toString())
    }
    router.post("/elements").handler { ctx ->
      ctx.request().bodyHandler { buf ->
        val updated = buf.toJsonArray().mapNotNull { if (it is JsonObject) it.getString("id") else null }
          .associateBy({ it }, { if (Math.random() < 0.01) "DEAD" else "ALIVE" })
        ctx.response().end(JsonObject(updated).toString())
      }
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
