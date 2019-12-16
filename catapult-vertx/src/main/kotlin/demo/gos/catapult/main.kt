package demo.gos.catapult

import io.vertx.core.Vertx

/**
 * @author Joel Takvorian
 */


fun main() {
  Vertx.vertx().deployVerticle(CatapultVerticle())
}
