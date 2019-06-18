package demo.gos.gos_villains

import io.vertx.core.Vertx

/**
 * @author Joel Takvorian
 */


fun main() {
  Vertx.vertx().deployVerticle(MockedBattlefield())
  Vertx.vertx().deployVerticle(Villains())
}
