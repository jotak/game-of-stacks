package demo.gos.villains

import demo.gos.common.ElementStatus
import demo.gos.common.ElementType
import demo.gos.common.Point
import io.vertx.core.json.JsonObject

data class Villain(val id: String, var pos: Point, var status: ElementStatus, var target: Target?) {
  companion object {
    fun fromJson(json: JsonObject): Villain {
      return Villain(json.getString("id"), Point(json.getDouble("x"), json.getDouble("y")), ElementStatus.valueOf(json.getString("status")), null)
    }

  }

  fun toJson(): JsonObject {
    return JsonObject().put("id", id).put("x", pos.x()).put("y", pos.y()).put("type", ElementType.VILLAIN).put("status", status)
  }

  fun isOnTarget(): Boolean {
    val diff = target?.pos?.diff(pos)
    // use a shot range
    return diff != null && diff.size() <= SHOT_RANGE
  }
}
