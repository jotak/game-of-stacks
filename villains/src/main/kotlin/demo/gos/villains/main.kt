package demo.gos.villains

import demo.gos.common.Commons
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx

/**
 * @author Joel Takvorian
 */


fun main() {
  //Vertx.vertx().deployVerticle(MockedBattlefield())
  Vertx.clusteredVertx(Commons.vertxOptions().setClustered(true)) { ar: AsyncResult<Vertx> -> ar.result().deployVerticle(VillainsVerticle()) }
}
