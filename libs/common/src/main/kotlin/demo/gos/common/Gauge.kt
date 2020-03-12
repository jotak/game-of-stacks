package demo.gos.common

class Gauge(private val size: Double, private val onFilled: () -> Unit) {
  private var pos = 0.0
  fun add(delta: Double) {
    pos += delta
    if (pos >= size) {
      onFilled()
      reset()
    }
  }

  fun get(): Double { return pos }

  fun reset() {
    pos = 0.0
  }
}
