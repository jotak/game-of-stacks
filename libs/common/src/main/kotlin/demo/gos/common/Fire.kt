package demo.gos.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Fire @JsonCreator constructor(
  @JsonProperty("source") val source: Noise,
  @JsonProperty("target") val target: Noise,
  @JsonProperty("accuracy") val accuracy: Double,
  @JsonProperty("range") val range: Double
)
