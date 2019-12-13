package demo.gos.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Noise @JsonCreator constructor(
        @JsonProperty("source") val source: String,
        @JsonProperty("x") val x: Double,
        @JsonProperty("y") val y: Double
)