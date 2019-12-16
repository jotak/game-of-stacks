package demo.gos.catapult

import demo.gos.common.Commons
import demo.gos.common.maths.Point
import demo.gos.common.maths.Segment
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import io.vertx.kotlin.kafka.client.producer.writeAwait
import java.util.*

class Boulder(vertx: Vertx, initPos: Point, private val destPos: Point, private val speed: Double, private val impactZone: Double) {
  private var curPos: Point = initPos
  private val id = "BOULDER-VX-" + UUID.randomUUID().toString()
  private var exploding = false
  private val kafkaProducer = KafkaProducer.create<String, JsonObject>(vertx, Commons.kafkaConfigProducer)

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
    kotlin.runCatching {
      val json = JsonObject().put("x", curPos.x()).put("y", curPos.y()).put("r", impactZone)
      kafkaProducer.writeAwait(KafkaProducerRecord.create("kill-around", json))
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
      kafkaProducer.writeAwait(KafkaProducerRecord.create("display", json))
    }.onFailure {
      LOGGER.error("Display error", it)
    }
  }
}
