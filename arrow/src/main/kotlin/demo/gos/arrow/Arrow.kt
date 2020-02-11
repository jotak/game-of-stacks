package demo.gos.arrow

import demo.gos.common.Fire
import io.smallrye.reactive.messaging.annotations.Channel
import io.smallrye.reactive.messaging.annotations.Emitter
import io.vertx.core.json.JsonObject
import java.util.*
import java.util.logging.Logger
import javax.inject.Inject
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.core.MediaType


@Path("arrow")
class Arrow {
    companion object {
        val LOG: Logger = Logger.getLogger(Arrow::class.java.name)
    }

    val id = "ARROW-Q-" + UUID.randomUUID().toString()


    @Inject
    @Channel("display")
    lateinit var displayEmitter: Emitter<JsonObject>

    @Inject
    @Channel("kill-single")
    lateinit var killSingleEmitter: Emitter<JsonObject>

    @Path("/fire")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun fire(fire: Fire) {
        killSingleEmitter.send(JsonObject().put("id", fire.target.id))
    }

    private fun display(fire: Fire) {
        //TODO: find a way to display it
    }

}
