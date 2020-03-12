package demo.gos.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import demo.gos.common.maths.Point
import demo.gos.common.maths.Segment

data class Circle @JsonCreator constructor(
  @JsonProperty("x") val x: Double,
  @JsonProperty("y") val y: Double,
  @JsonProperty("r") val r: Double
) {
  companion object {
    @JvmStatic fun fromCenter(p: Point, r: Double): Circle {
      return Circle(p.x(), p.y(), r)
    }
  }

  fun center(): Point {
    return Point(x, y)
  }

  fun contains(pos: Point): Boolean {
    return Segment(pos, center()).size() <= r
  }
}
