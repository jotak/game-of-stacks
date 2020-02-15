package demo.gos.arrow

import demo.gos.common.DisplayData
import demo.gos.common.Fire
import demo.gos.common.Noise
import demo.gos.common.Tween
import demo.gos.common.maths.Segment
import io.smallrye.reactive.messaging.annotations.Channel
import io.smallrye.reactive.messaging.annotations.Emitter
import io.vertx.core.json.JsonObject
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.security.SecureRandom
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
    val rnd = SecureRandom()

    @ConfigProperty(name = "accuracy", defaultValue = "0.5")
    var accuracy: Double = 0.0

    @ConfigProperty(name = "range", defaultValue = "400")
    var range: Double = 400.0

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
        // Is in range?
        val segToDest = Segment(fire.source.toPoint(), fire.target.toPoint())
        val shootSize = segToDest.size()
        if (shootSize < range) {
            if(rnd.nextDouble() < accuracy) {
                killSingleEmitter.send(JsonObject().put("id", fire.target.id))
            }
            display(fire)
        } else {
            // not in range
            val dest = segToDest.derivate().normalize().mult(range).add(fire.source.toPoint())
            display(Fire(fire.source, Noise(fire.target.id, dest.x(), dest.y())))
        }
    }

    private fun display(fire: Fire) {
        val data = DisplayData(
                id = "arrow-" + UUID.randomUUID(),
                x = fire.source.x,
                y = fire.source.y,
                sprite = "arrow",
                tween = Tween(
                        x = fire.target.x,
                        y = fire.target.y,
                        time = 300.0
                )
        )
        val json = JsonObject.mapFrom(data)
        displayEmitter.send(json)
    }

}
