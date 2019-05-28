package demo.gos.gos_villains

import demo.gos.common.Commons
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future

val CROWD_SIZE = Commons.getIntEnv("CROWD_SIZE", 20)

data class Villain(val id: String)

class Villains : AbstractVerticle() {
  private val villains: List<Villain> = (1..CROWD_SIZE).map { Villain("VILLAIN-$it") }

  override fun start(startFuture: Future<Void>) {
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
}
