package demo.gos.common

import demo.gos.common.maths.Point
import java.security.SecureRandom

data class Area(val x: Double, val y: Double, val width: Double, val height: Double) {
  fun spawn(rnd: SecureRandom): Point {
    return Point(x + rnd.nextDouble() * width, y + rnd.nextDouble() * height)
  }

  fun fitInto(p: Point): Point {
    val x = if (p.x() < x) x else if (p.x() > x + width) x + width else p.x()
    val y = if (p.y() < y) y else if (p.y() > y + height) y + height else p.y()
    return Point(x, y)
  }
}
