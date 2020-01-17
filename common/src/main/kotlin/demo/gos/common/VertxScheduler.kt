package demo.gos.common

import io.vertx.core.Vertx
import java.util.concurrent.atomic.AtomicReference

class VertxScheduler(val vertx: Vertx) {

  private val timerId = AtomicReference<Long>()

  fun schedule(interval: Long, handler: () -> Any) {
    timerId.getAndUpdate { id ->
      if (id != null) {
        vertx.cancelTimer(id)
      }
      return@getAndUpdate vertx.setPeriodic(interval) {
        vertx.executeBlocking<Void>({
          handler()
          it.complete()
        }, {})
      }
    }
  }

  fun cancel() {
    timerId.getAndUpdate {
      if (it != null) {
        vertx.cancelTimer(it)
      }
      return@getAndUpdate null
    }

  }


}
