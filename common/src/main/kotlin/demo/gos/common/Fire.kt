package demo.gos.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import demo.gos.common.Noise

data class Fire @JsonCreator constructor(@JsonProperty("source") val source: Noise, @JsonProperty("target") val target: Noise)
