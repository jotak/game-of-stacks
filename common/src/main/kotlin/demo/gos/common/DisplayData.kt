package demo.gos.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class DisplayData @JsonCreator constructor(
  @JsonProperty("id") val id: String,
  @JsonProperty("x") val x: Double,
  @JsonProperty("y") val y: Double,
  @JsonProperty("sprite") val sprite: String,
  @JsonProperty("action") val action: String? = null,
  @JsonProperty("value") val value: Double? = null
)
