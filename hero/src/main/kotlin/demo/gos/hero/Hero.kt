package demo.gos.hero

import demo.gos.common.*
import io.quarkus.runtime.StartupEvent
import io.quarkus.scheduler.Scheduled
import io.smallrye.reactive.messaging.annotations.Channel
import io.smallrye.reactive.messaging.annotations.Emitter
import io.vertx.core.json.JsonObject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Incoming
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Logger
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton


@Singleton
class Hero {
    companion object {
        val LOG: Logger = Logger.getLogger(Hero::class.java.name)
        val HEROES = listOf(
                Position("Aria Stark", 500.0, 400.0),
                Position("Ned Stark",  500.0, 200.0),
                Position("John Snow",  500.0, 100.0),
                Position("Daenerys Targaryen", 400.0, 400.0)
        )
    }

    @ConfigProperty(name = "num", defaultValue = "0")
    lateinit var num: Provider<Int>

    lateinit var id: String


    val position = AtomicReference<Point>(Point(500.0, 400.0))
    val dead = AtomicBoolean(false)
    val paused = AtomicBoolean(false)


    @Inject
    @Channel("hero-making-noise")
    lateinit var emitter: Emitter<JsonObject>

    fun onStart(@Observes e: StartupEvent){
        reset()
        LOG.info("${id} has joined the game (${position.get().x()}, ${position.get().y()})")
    }

    @Scheduled(every = "5s")
    fun scheduled() {
        if(!paused.get()) {
            makeNoise()
        }
    }

    @Incoming("game")
    fun game(e: String) {
       LOG.info(e)
        when(e) {
            "play" -> paused.set(false)
            "pause" -> paused.set(true)
            "reset" -> reset()
        }
    }

    private fun reset() {
        val hero = HEROES[num.get()]
        paused.set(false)
        dead.set(false)
        id = hero.id
        position.set(Point(hero.x, hero.y))
    }

    @Incoming("villain-making-noise")
    fun villainMakingNoise(o: JsonObject) {
        val noise = o.mapTo(Position::class.java)
        LOG.info("received: $noise")
    }

    fun makeNoise() {
        emitter.send(JsonObject.mapFrom(Position(id, position.get().x(), position.get().y())))
    }

}