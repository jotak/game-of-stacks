package demo.gos.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import demo.gos.common.maths.Point

data class GameCommand @JsonCreator constructor(
  @JsonProperty("type") val type: String
)
