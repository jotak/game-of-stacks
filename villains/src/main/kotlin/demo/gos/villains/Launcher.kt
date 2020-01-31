package demo.gos.villains

import demo.gos.common.Commons
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.impl.launcher.VertxCommandLauncher
import io.vertx.core.impl.launcher.VertxLifecycleHooks
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.micrometer.micrometerMetricsOptionsOf
import io.vertx.kotlin.micrometer.vertxPrometheusOptionsOf
import io.vertx.micrometer.Label
import io.vertx.micrometer.backends.BackendRegistries
import java.util.*

/**
 * @author Joel Takvorian
 */
class Launcher : VertxCommandLauncher(), VertxLifecycleHooks {
  companion object {
    @JvmStatic
    fun main(vararg args: String) {
      Launcher().dispatch(args)
    }
  }

  override fun handleDeployFailed(vertx: Vertx?, mainVerticle: String?, deploymentOptions: DeploymentOptions?, cause: Throwable?) {
  }

  override fun beforeStartingVertx(options: VertxOptions?) {
    if (Commons.metricsEnabled == 1) {
      options?.metricsOptions = micrometerMetricsOptionsOf()
        .setPrometheusOptions(vertxPrometheusOptionsOf()
          .setStartEmbeddedServer(true)
          .setEmbeddedServerOptions(HttpServerOptions().setPort(9090))
          .setPublishQuantiles(true)
          .setEnabled(true))
        .setLabels(EnumSet.of(Label.POOL_TYPE, Label.POOL_NAME, Label.CLASS_NAME, Label.HTTP_CODE, Label.HTTP_METHOD, Label.HTTP_PATH, Label.EB_ADDRESS, Label.EB_FAILURE, Label.EB_SIDE))
        .setEnabled(true)
    }
  }

  override fun afterStartingVertx(vertx: Vertx?) {
    if (Commons.metricsEnabled == 1) {
      // Instrument JVM
      val registry = BackendRegistries.getDefaultNow()
      if (registry != null) {
        ClassLoaderMetrics().bindTo(registry)
        JvmMemoryMetrics().bindTo(registry)
        JvmThreadMetrics().bindTo(registry)
      }
    }
  }

  override fun afterStoppingVertx() {
  }

  override fun afterConfigParsed(config: JsonObject?) {
  }

  override fun beforeStoppingVertx(vertx: Vertx?) {
  }

  override fun beforeDeployingVerticle(deploymentOptions: DeploymentOptions?) {
  }
}
