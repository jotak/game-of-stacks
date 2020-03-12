package demo.gos.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Tween @JsonCreator constructor(
  @JsonProperty("time") val time: Double,
  @JsonProperty("x") val x: Double,
  @JsonProperty("y") val y: Double
)
