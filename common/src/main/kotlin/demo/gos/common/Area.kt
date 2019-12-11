package demo.gos.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class Area @JsonCreator constructor(
        @JsonProperty("name") val name: String,
        @JsonProperty("x") val x: Double,
        @JsonProperty("y") val y: Double,
        @JsonProperty("width") val width: Double,
        @JsonProperty("height") val height: Double
)