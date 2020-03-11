package demo.gos.catapult

import demo.gos.common.Circle
import demo.gos.common.DisplayData
import demo.gos.common.maths.Point
import io.smallrye.reactive.messaging.annotations.Emitter
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.*

class Boulder(id: String, private val displayEmitter: Emitter<JsonArray>, private val killAroundEmitter: Emitter<JsonObject>, initPos: Point, destPos: Point, speed: Double, impactZone: Double)
    : BaseBoulder(id, initPos, destPos, speed, impactZone) {

  override suspend fun killAround(zone: Circle) {
    killAroundEmitter.send(JsonObject.mapFrom(zone))
  }

  override suspend fun display(data: DisplayData) {
    displayEmitter.send(JsonArray(listOf(data)))
  }
}
