package demo.gos.gm

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

abstract class Action {
    companion object {
        @JsonCreator
        @JvmStatic
        fun factory(@JsonProperty("id") id: String?,
                    @JsonProperty("x") x: Double?,
                    @JsonProperty("y") y: Double?,
                    @JsonProperty("targetId") targetId: String?,
                    @JsonProperty("killerId") killerId: String?,
                    @JsonProperty("kamikaze") kamikaze: Boolean?): Action {
            return if (killerId != null && targetId != null) {
                KillAction(killerId, targetId, kamikaze ?: false)
            } else if (id != null && x != null && y != null) {
                MoveAction(id, x, y)
            } else {
                throw IllegalStateException("Invalid action format")
            }
        }
    }
}

data class MoveAction(
        val id: String,
        val x: Double,
        val y: Double
) : Action()

data class KillAction constructor(
        val killerId: String,
        val targetId: String,
        val kamikaze: Boolean
) : Action()

