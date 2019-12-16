package demo.gos.common

import demo.gos.common.maths.Point
import java.security.SecureRandom

data class Area(val x: Double, val y: Double, val width: Double, val height: Double) {
  fun spawn(rnd: SecureRandom): Point {
    return Point(x + rnd.nextDouble() * width, y + rnd.nextDouble() * height)
  }
}
