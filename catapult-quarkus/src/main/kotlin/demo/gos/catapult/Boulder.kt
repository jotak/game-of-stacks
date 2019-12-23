package demo.gos.catapult

import demo.gos.common.Circle
import demo.gos.common.DisplayData
import demo.gos.common.maths.Point
import io.smallrye.reactive.messaging.annotations.Channel
import io.smallrye.reactive.messaging.annotations.Emitter
import io.vertx.core.json.JsonObject
import java.util.*
import javax.inject.Inject

class Boulder(initPos: Point, destPos: Point, speed: Double, impactZone: Double)
    : BaseBoulder("BOULDER-Q-" + UUID.randomUUID().toString(), initPos, destPos, speed, impactZone) {

  @Inject
  @Channel("kill-around")
  lateinit var killAroundEmitter: Emitter<JsonObject>

  @Inject
  @Channel("display")
  lateinit var displayEmitter: Emitter<JsonObject>

  override suspend fun killAround(zone: Circle) {
    killAroundEmitter.send(JsonObject.mapFrom(zone))
  }

  override suspend fun display(data: DisplayData) {
    displayEmitter.send(JsonObject.mapFrom(data))
  }
}
