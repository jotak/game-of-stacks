package demo.gos.hero

import demo.gos.common.Areas
import demo.gos.common.Noise
import demo.gos.common.Players
import demo.gos.common.maths.Point
import io.quarkus.runtime.StartupEvent
import io.quarkus.scheduler.Scheduled
import io.smallrye.reactive.messaging.annotations.Channel
import io.smallrye.reactive.messaging.annotations.Emitter
import io.vertx.core.json.JsonObject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Incoming
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Logger
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

val RND = SecureRandom()

@Singleton
class Hero {
    companion object {
        val LOG: Logger = Logger.getLogger(Hero::class.java.name)
        const val DELTA_MS = 300L
        val HEROES = mapOf(
                "aria" to "Aria-Stark",
                "ned" to "Ned-Stark",
                "john" to "John-Snow",
                "deany" to "Daenerys-Targaryen"
        )
    }

    @ConfigProperty(name = "shortId", defaultValue = "aria")
    lateinit var shortId: Provider<String>

    @ConfigProperty(name = "accuracy", defaultValue = "0.7")
    lateinit var accuracy: Provider<Double>

    @ConfigProperty(name = "speed", defaultValue = "35.0")
    lateinit var speed: Provider<Double>

    lateinit var id: String


    private val position = AtomicReference<Point>(Areas.spawnHeroesArea.spawn(RND))
    private val dead = AtomicBoolean(false)
    private val paused = AtomicBoolean(false)
    private val targetWeapon = AtomicReference<Point>()


    @Inject
    @Channel("hero-making-noise")
    lateinit var heroNoiseEmitter: Emitter<JsonObject>

    @Inject
    @Channel("display")
    lateinit var displayEmitter: Emitter<JsonObject>

    fun onStart(@Observes e: StartupEvent) {
        reset()
        LOG.info("$id has joined the game (${position.get().x()}, ${position.get().y()})")
    }

    @Scheduled(every = "0.3s")
    fun scheduled() {
        if (!paused.get()) {
            makeNoise()
            if (targetWeapon.get() != null && !isOnWeapon()) {
                walk()
            }
            display()
        }
    }

    private fun walk() {
        position.set(Players.walk(
                rnd = RND,
                pos = position.get(),
                dest = targetWeapon.get(),
                accuracy = accuracy.get(),
                speed = speed.get(),
                delta = DELTA_MS.toDouble() / 1000
        ))

        LOG.info("$id at ${position.get()} walking toward ${targetWeapon.get()}")
    }

    private fun isOnWeapon(): Boolean {
        return targetWeapon.get()?.equals(position.get()) ?: false
    }

    private fun display() {
        val color = if (dead.get()) "#802020" else "#101030"
        val json = JsonObject()
                .put("id", id)
                .put("style", "position: absolute; background-color: $color; transition: top ${DELTA_MS}ms, left ${DELTA_MS}ms; height: 30px; width: 30px; z-index: 8;")
                .put("text", "")
                .put("x", position.get().x() - 15)
                .put("y", position.get().y() - 15)
        displayEmitter.send(json)
    }

    @Incoming("game")
    fun game(o: JsonObject) {
        val type = o.getString("type")!!
        LOG.info("$id received $type")
        when (type) {
            "play" -> paused.set(false)
            "pause" -> paused.set(true)
            "reset" -> reset()
        }
    }

    private fun reset() {
        id = HEROES.getValue(shortId.get())
        paused.set(false)
        dead.set(false)
        position.set(Areas.spawnHeroesArea.spawn(RND))
    }

    @Incoming("villain-making-noise")
    fun villainMakingNoise(o: JsonObject) {
        val noise = o.mapTo(Noise::class.java)
        LOG.finest("$id  received villain noise: $noise")
    }

    @Incoming("weapon-making-noise")
    fun weaponMakingNoise(o: JsonObject) {
        val noise = o.mapTo(Noise::class.java)
        LOG.finest("$id received weapon noise: $noise")
        targetWeapon.compareAndSet(null, noise.toPoint())
    }

    fun makeNoise() {
        heroNoiseEmitter.send(JsonObject.mapFrom(Noise.fromPoint(id, position.get())))
    }

}