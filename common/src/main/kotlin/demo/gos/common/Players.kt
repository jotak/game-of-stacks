package demo.gos.common

import demo.gos.common.maths.Point
import demo.gos.common.maths.Segment
import java.security.SecureRandom
import kotlin.math.min

object Players {
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
}
