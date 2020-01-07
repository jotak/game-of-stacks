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
    val data = if (exploding) {
      val txDur = "0.5s"
      val size = impactZone.toInt()
      val style = "position: absolute; background-color: rgba(255,0,0,0); " +
        "transition-timing-function: ease-out; " +
        "transition: top $txDur, left $txDur, width $txDur, height $txDur, background-color $txDur; height: ${size}px; width: ${size}px; " +
        "border-radius: 50%; z-index: 8;"
      DisplayData(id, curPos.x() - size / 2, curPos.y() - size / 2, style, "")
    } else {
      val style = "position: absolute; background-color: #45282C; transition: top ${DELTA_MS}ms, left ${DELTA_MS}ms; height: 20px; width: 20px; border-radius: 50%; z-index: 8;"
      DisplayData(id, curPos.x() - 10, curPos.y() - 10, style, "")
    }
    display(data)
  }
}
