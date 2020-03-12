package demo.gos.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import demo.gos.common.maths.Point

data class Noise @JsonCreator constructor(
  @JsonProperty("id") val id: String,
  @JsonProperty("x") val x: Double,
  @JsonProperty("y") val y: Double
) {
  companion object {
    @JvmStatic fun fromPoint(id: String, p: Point): Noise {
      return Noise(id, p.x(), p.y())
    }
  }

  fun toPoint(): Point {
    return Point(x, y)
  }
}
