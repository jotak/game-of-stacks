package demo.gos.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class Element @JsonCreator constructor(
        @JsonProperty("id") val id: String,
        @JsonProperty("type") val type: ElementType,
        @JsonProperty("x") val x: Double,
        @JsonProperty("y") val y: Double,
        @JsonProperty("status") val status: ElementStatus?
)