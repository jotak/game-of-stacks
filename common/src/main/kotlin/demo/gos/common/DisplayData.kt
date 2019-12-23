package demo.gos.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class DisplayData @JsonCreator constructor(
  @JsonProperty("id") val id: String,
  @JsonProperty("x") val x: Double,
  @JsonProperty("y") val y: Double,
  @JsonProperty("style") val style: String,
  @JsonProperty("text") val text: String
)
