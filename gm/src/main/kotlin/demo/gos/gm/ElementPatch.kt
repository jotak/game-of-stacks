package demo.gos.gm

import javax.json.bind.annotation.JsonbCreator
import javax.json.bind.annotation.JsonbProperty

data class ElementPatch @JsonbCreator constructor(
        @JsonbProperty("id") val id: String,
        @JsonbProperty("x") val x: Double,
        @JsonbProperty("y") val y: Double,
        @JsonbProperty("status") val status: ElementStatus?
)
