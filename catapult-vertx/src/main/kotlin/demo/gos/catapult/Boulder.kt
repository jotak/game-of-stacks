package demo.gos.catapult

import demo.gos.common.Circle
import demo.gos.common.Commons
import demo.gos.common.DisplayData
import demo.gos.common.maths.Point
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import io.vertx.kotlin.kafka.client.producer.writeAwait
import java.util.*

class Boulder(id: String, vertx: Vertx, initPos: Point, destPos: Point, speed: Double, impactZone: Double)
    : BaseBoulder(id, initPos, destPos, speed, impactZone) {
  private val kafkaProducer = KafkaProducer.create<String, JsonObject>(vertx, Commons.kafkaConfigProducer)
  private val kafkaDisplayProducer = KafkaProducer.create<String, JsonArray>(vertx, Commons.kafkaArrayConfigProducer)

  override suspend fun killAround(zone: Circle) {
    kotlin.runCatching {
      val json = JsonObject.mapFrom(zone)
      kafkaProducer.writeAwait(KafkaProducerRecord.create("kill-around", json))
    }.onFailure {
      LOGGER.error("Boom error", it)
    }
  }

  override suspend fun display(data: DisplayData) {
    kotlin.runCatching {
      kafkaDisplayProducer.writeAwait(KafkaProducerRecord.create("display", JsonArray(listOf(data))))
    }.onFailure {
      LOGGER.error("Display error", it)
    }
  }
}
