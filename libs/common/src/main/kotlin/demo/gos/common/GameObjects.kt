package demo.gos.common

import demo.gos.common.maths.Point
import java.security.SecureRandom

object GameObjects {
  fun startingPoint(rnd: SecureRandom, area: Area, x: Double?, y: Double?): Point {
    var pos = area.spawn(rnd)
    if (x != null) {
      pos = Point(x, pos.y())
    }
    if (y != null) {
      pos = Point(pos.x(), y)
    }
    return pos
  }
}
