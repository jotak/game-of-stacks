package demo.gos.common

class Throughput(private val name: String, private val onTick: (Long) -> Unit) {
  private var lastTick = System.nanoTime()
  private var counter = 0L
  companion object {
    fun Printer(name: String): Throughput {
      return Throughput(name, fun(c: Long) { println("$name: $c ops") })
    }
  }
  fun mark() {
    val now = System.nanoTime()
    val elapsed = now - lastTick
    counter++
    if (elapsed >= 1_000_000_000) {
      onTick(counter)
      lastTick = now
      counter = 0
    }
  }
}
