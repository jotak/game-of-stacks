package demo.gos.catapult

import demo.gos.common.*
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendJsonAwait
import java.util.*

class Boulder(private val vertx: Vertx, private val parentId: String, initPos: Point, private val destPos: Point, private val speed: Double, private val impactZone: Double) {
  private var curPos: Point = initPos
  private val id = "BOULDER-VX-" + UUID.randomUUID().toString()
  private var exploding = false

  suspend fun update(delta: Double): Boolean {
    val segToDest = Segment(curPos, destPos)
    val speed = delta * speed
    val step = segToDest.derivate().normalize().mult(speed)
    var ret = false
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
    LOGGER.info("BOOM!")
    exploding = true
    // Get all villains
    kotlin.runCatching {
      val res = WebClient.create(vertx).get(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/gm/elements")
        .addQueryParam("type", ElementType.VILLAIN.toString())
        .addQueryParam("status", ElementStatus.ALIVE.toString()).sendAwait()

      val dead = res.bodyAsJsonArray().mapNotNull { if (it is JsonObject) it else null }
        .filter {
          val p = Point(it.getDouble("x"), it.getDouble("y"))
          curPos.diff(p).size() <= impactZone
        }.map {
          JsonObject().put("killerId", parentId).put("targetId", it.getString("id"))
        }

      LOGGER.info("Killing ${dead.size} !!")
      WebClient.create(vertx).patch(Commons.BATTLEFIELD_PORT, Commons.BATTLEFIELD_HOST, "/gm/action/batch").sendJsonAwait(JsonArray(dead))
    }.onFailure {
      LOGGER.error("Boom error", it)
    }
  }

  private suspend fun display() {
    val json = JsonObject()
      .put("id", id)
      .put("style", "position: absolute; background-color: #25383C; transition: top ${DELTA_MS}ms, left ${DELTA_MS}ms; height: 20px; width: 20px; border-radius: 50%; z-index: 8;")
      .put("text", if (exploding) "BOOM" else "")
      .put("x", curPos.x() - 10)
      .put("y", curPos.y() - 10)

    kotlin.runCatching {
      WebClient.create(vertx).post(Commons.UI_PORT, Commons.UI_HOST, "/display").sendJsonAwait(json)
    }.onFailure {
      LOGGER.error("Display error", it)
    }
  }
}
