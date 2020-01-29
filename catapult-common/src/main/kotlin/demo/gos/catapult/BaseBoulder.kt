package demo.gos.catapult

import demo.gos.common.Circle
import demo.gos.common.DisplayData
import demo.gos.common.maths.Point
import demo.gos.common.maths.Segment

abstract class BaseBoulder(private val id: String, initPos: Point, private val destPos: Point, private val speed: Double, private val impactZone: Double) {
  private var curPos: Point = initPos
  private var exploding = false

  suspend fun update(delta: Double): Boolean {
    var ret = false
    val segToDest = Segment(curPos, destPos)
    val speed = delta * speed
    val step = segToDest.derivate().normalize().mult(speed)
    if (step.size() >= segToDest.size()) {
      // Reached dest, BOOM!
      curPos = destPos
      boom()
      ret = true
    } else {
      curPos = curPos.add(step)
    }
    display()
    return ret
  }

  private suspend fun boom() {
    exploding = true
    killAround(Circle.fromCenter(curPos, impactZone))
  }

  abstract suspend fun killAround(zone: Circle)

  abstract suspend fun display(data: DisplayData)

  private suspend fun display() {
    val sprite = if (exploding) "explode" else "boulder"
    display(DisplayData(id = id, x = curPos.x(), y = curPos.y(), sprite = sprite))
  }
}
