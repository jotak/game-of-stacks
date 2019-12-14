package demo.gos.common

import java.security.SecureRandom
import kotlin.math.min

object Players {
    private val RND = SecureRandom()

    fun walk(pos: Point, dest: Point, speed: Double, accuracy: Double, delta: Double): Point {
        // Speed and angle are modified by accuracy
        val segToDest = Segment(pos, dest)
        // maxSpeed avoids stepping too high when close to destination
        val maxSpeed = min(segToDest.size() / delta, speed)
        // minSpeed must be kept <= maxSpeed
        val minSpeed = min(maxSpeed, accuracy * speed)
        val speed = delta * (minSpeed + RND.nextDouble() * (maxSpeed - minSpeed))
        val relativeMove = randomishSegmentNormalized(accuracy, segToDest).mult(speed)
        return pos.add(relativeMove)
    }

    fun spawnIn(area: Area): Point {
        return Point(area.x + RND.nextDouble() * area.width, area.y + RND.nextDouble() * area.height)
    }

    private fun randomishSegmentNormalized(accuracy: Double, segToDest: Segment): Point {
        var angle = RND.nextDouble() * (1.0 - accuracy) * Math.PI
        if (RND.nextInt(2) == 0) {
            angle *= -1
        }
        return segToDest.derivate().normalize().rotate(angle)
    }
}