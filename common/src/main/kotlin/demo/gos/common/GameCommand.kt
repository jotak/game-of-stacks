package demo.gos.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class GameCommand @JsonCreator constructor(
  @JsonProperty("type") val type: String
)
