package demo.gos.common

import demo.gos.common.maths.Point
import demo.gos.common.maths.Segment
import org.apache.commons.lang3.RandomStringUtils
import java.security.SecureRandom
import kotlin.math.min

object Players {

  fun randomName(rnd: SecureRandom): String {
    return RandomStringUtils.random(3, 0, 0, true, true, null, rnd);
  }

  fun walk(rnd: SecureRandom, pos: Point, dest: Point, speed: Double, accuracy: Double, delta: Double): Point {
    // Speed and angle are modified by accuracy
    val segToDest = Segment(pos, dest)
    // maxSpeed avoids stepping too high when close to destination
    val maxSpeed = min(segToDest.size() / delta, speed)
    // minSpeed must be kept <= maxSpeed
    val minSpeed = min(maxSpeed, accuracy * speed)
    val actualSpeed = delta * (minSpeed + rnd.nextDouble() * (maxSpeed - minSpeed))
    val relativeMove = randomishSegmentNormalized(rnd, accuracy, segToDest).mult(actualSpeed)
    return pos.add(relativeMove)
  }

  private fun randomishSegmentNormalized(rnd: SecureRandom, accuracy: Double, segToDest: Segment): Point {
    var angle = rnd.nextDouble() * (1.0 - accuracy) * Math.PI
    if (rnd.nextInt(2) == 0) {
      angle *= -1
    }
    return segToDest.derivate().normalize().rotate(angle)
  }

  fun walkRandom(rnd: SecureRandom, pos: Point, dest: Point?, speed: Double, accuracy: Double, delta: Double, area: Area? = null): Pair<Point, Point> {
    val newDest = pickRandomDest(rnd, pos, dest, area)
    val newPos = walk(rnd, pos, newDest, speed, accuracy, delta)
    return Pair(newPos, newDest)
  }

  private fun pickRandomDest(rnd: SecureRandom, pos: Point, dest: Point?, area: Area?): Point {
    return if (dest != null && pos.diff(dest).size() > 20) {
      // Not arrived yet => continue to walk to previously picked random destination
      return dest
    } else {
      // Else, pick a new random destination close to current position
      if(area != null) {
        return Areas.spawnHeroesArea.spawn(rnd)
      }
      return Point(rnd.nextDouble() * 100, rnd.nextDouble() * 100).diff(Point(50.0, 50.0)).add(pos)
    }
  }
}
