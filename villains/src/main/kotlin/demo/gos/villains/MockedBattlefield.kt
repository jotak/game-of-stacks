package demo.gos.villains

import demo.gos.common.Commons
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router

class MockedBattlefield : AbstractVerticle() {
  private val heroes = JsonArray().add(JsonObject().put("id", "Ned Stark").put("x", 500.0).put("y", 400.0))
  private val villains = JsonArray()

  override fun start(startFuture: Future<Void>) {
    val serverOptions = HttpServerOptions().setPort(Commons.BATTLEFIELD_PORT)
    val router = Router.router(vertx)
    router.get("/gm/elements").handler {
      if (it.request().getParam("type") == "VILLAIN") {
        it.response().end(villains.toString())
      } else {
        it.response().end(heroes.toString())
      }
    }
    router.post("/gm/element/batch").handler { ctx ->
      ctx.request().bodyHandler { buf ->
        villains.addAll(buf.toJsonArray())
        ctx.response().end("")
      }
    }
    router.patch("/gm/element/batch").handler { ctx ->
      ctx.request().bodyHandler { buf ->
        val arr = buf.toJsonArray()
        arr.forEach {
          if (it is JsonObject) {
            it.put("status", if (Math.random() < 0.01) "DEAD" else "ALIVE")
            villains.forEach { it2 ->
              if (it2 is JsonObject && it2.getString("id") == it.getString("id")) {
                it2.put("x", it.getValue("x"))
                it2.put("y", it.getValue("y"))
                it2.put("status", it.getValue("status"))
              }
            }
          }
        }
        ctx.response().end(arr.toString())
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
