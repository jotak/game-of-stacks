package demo.gos.common

import demo.gos.common.maths.Point
import demo.gos.common.maths.Segment

class PerceivedNoise(val noise: Noise, private val distCoef: Double) {
  private var fading = 1

  companion object {
    @JvmStatic val maxSize = Segment(
      Point(Areas.arena.x, Areas.arena.y),
      Point(Areas.arena.x + Areas.arena.width, Areas.arena.y + Areas.arena.height))
      .size()

    @JvmStatic fun create(noise: Noise, fromPos: Point): PerceivedNoise {
      val distCoef = Segment(noise.toPoint(), fromPos).size() / maxSize
      return PerceivedNoise(noise, distCoef)
    }
  }

  fun fade() {
    fading++
  }

  fun isStrongerThan(other: PerceivedNoise): Boolean {
    return (fading * distCoef) <= (other.fading * other.distCoef)
  }
}
