package demo.gos.heroes

import demo.gos.common.*
import io.quarkus.scheduler.Scheduled
import io.smallrye.reactive.messaging.annotations.Broadcast
import io.smallrye.reactive.messaging.annotations.Channel
import io.smallrye.reactive.messaging.annotations.Emitter
import io.vertx.core.json.JsonObject
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Outgoing
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Hero {
    companion object {
        val LOG: Logger = Logger.getLogger(Hero::class.java.name)
        val HEROES = listOf(
                Element("Aria Stark", ElementType.HERO, 500.0, 400.0, ElementStatus.ALIVE),
                Element("Ned Stark", ElementType.HERO, 500.0, 200.0, ElementStatus.ALIVE),
                Element("John Snow", ElementType.HERO, 500.0, 100.0, ElementStatus.ALIVE),
                Element("Daenerys Targaryen", ElementType.HERO, 400.0, 400.0, ElementStatus.ALIVE)
        )
    }

    val id = "Aria Stark"
    val position = AtomicReference<Point>(Point(500.0, 400.0))
    @Inject
    @Channel("hero-making-noise")
    lateinit var emitter: Emitter<JsonObject>


    @Scheduled(every = "5s")
    fun scheduled() {
        makeNoise()
    }

    @Incoming("game")
    fun game(e: String) {
       LOG.info(e)
    }

    @Incoming("villain-making-noise")
    fun villainMakingNoise(o: JsonObject) {
        val noise = o.mapTo(Noise::class.java)
        LOG.info("received: $noise")
    }

    fun makeNoise() {
        emitter.send(JsonObject.mapFrom(Noise(id, position.get().x(), position.get().y())))
    }

}