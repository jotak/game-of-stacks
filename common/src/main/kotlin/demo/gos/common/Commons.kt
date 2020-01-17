package demo.gos.common

import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.micrometer.Label
import io.vertx.micrometer.MicrometerMetricsOptions
import io.vertx.micrometer.VertxPrometheusOptions
import java.util.*

object Commons {
  private val metricsEnabled = getIntEnv("METRICS_ENABLED", 0)
  @JvmStatic val kafkaAddress = getStringEnv("KAFKA_ADDRESS", "localhost:9092")
  @JvmStatic val kafkaConfigProducer: Map<String, String> = mapOf(
    "bootstrap.servers" to kafkaAddress,
    "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
    "value.serializer" to "io.vertx.kafka.client.serialization.JsonObjectSerializer",
    "acks" to "0"
  )

  @JvmStatic val kafkaConfigConsumer = fun(groupId: String): Map<String, String> {
    return mapOf(
      "bootstrap.servers" to kafkaAddress,
      "group.id" to groupId,
      "key.deserializer" to "org.apache.kafka.common.serialization.StringDeserializer",
      "value.deserializer" to "io.vertx.kafka.client.serialization.JsonObjectDeserializer",
      "auto.offset.reset" to "latest",
      "enable.auto.commit" to "false"
    )
  }

  @JvmStatic fun getStringEnv(varname: String, def: String): String {
    val v = System.getenv(varname)
    return if (v == null || v.isEmpty()) {
      def
    } else {
      println("$varname = $v")
      v
    }
  }

  @JvmStatic fun getIntEnv(varname: String?, def: Int): Int {
    val v = System.getenv(varname)
    return if (v == null || v.isEmpty()) {
      def
    } else {
      println("$varname = $v")
      v.toInt()
    }
  }

  @JvmStatic fun getDoubleEnv(varname: String?, def: Double): Double {
    val v = System.getenv(varname)
    return if (v == null || v.isEmpty()) {
      def
    } else {
      println("$varname = $v")
      v.toDouble()
    }
  }

  @JvmStatic fun vertxOptions(): VertxOptions {
    return if (metricsEnabled == 1) {
      VertxOptions().setMetricsOptions(MicrometerMetricsOptions()
        .setPrometheusOptions(VertxPrometheusOptions()
          .setStartEmbeddedServer(true)
          .setEmbeddedServerOptions(HttpServerOptions().setPort(9090))
          .setPublishQuantiles(true)
          .setEnabled(true))
        .setLabels(EnumSet.of(Label.POOL_TYPE, Label.POOL_NAME, Label.CLASS_NAME, Label.HTTP_CODE, Label.HTTP_METHOD, Label.HTTP_PATH, Label.EB_ADDRESS, Label.EB_FAILURE, Label.EB_SIDE))
        .setEnabled(true))
    } else VertxOptions()
  }
}
