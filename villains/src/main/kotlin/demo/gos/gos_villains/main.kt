package demo.gos.gos_villains

import io.vertx.core.Vertx

/**
 * @author Joel Takvorian
 */


fun main() {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(Villains())
}
