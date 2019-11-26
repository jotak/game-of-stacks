package demo.gos.gm

import javax.json.bind.annotation.JsonbCreator
import javax.json.bind.annotation.JsonbProperty

data class Element @JsonbCreator constructor(
        @JsonbProperty("id") val id: String,
        @JsonbProperty("type") val type: ElementType,
        @JsonbProperty("x") val x: Double,
        @JsonbProperty("y") val y: Double,
        @JsonbProperty("status") val status: ElementStatus?
)