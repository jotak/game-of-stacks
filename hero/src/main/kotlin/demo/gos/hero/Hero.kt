package demo.gos.hero

import demo.gos.common.*
import demo.gos.common.maths.Point
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import io.smallrye.reactive.messaging.annotations.Channel
import io.smallrye.reactive.messaging.annotations.Emitter
import io.smallrye.reactive.messaging.annotations.OnOverflow
import io.vertx.core.Vertx
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

val X = System.getenv("X")?.toDouble()
val Y = System.getenv("Y")?.toDouble()

@Singleton
class Hero {
    companion object {
        val LOG: Logger = Logger.getLogger(Hero::class.java.name)
        const val DELTA_MS = 300L
        val HEROES = mapOf(
                "aria" to "Aria-Stark",
                "ned" to "Ned-Stark",
                "jon" to "Jon-Snow",
                "deany" to "Daenerys-Targaryen"
        )
    }

    @ConfigProperty(name = "shortId", defaultValue = "aria")
    lateinit var shortId: Provider<String>

    @ConfigProperty(name = "accuracy", defaultValue = "0.7")
    lateinit var accuracy: Provider<Double>

    @ConfigProperty(name = "speed", defaultValue = "35.0")
    lateinit var speed: Provider<Double>

    private val initialized = AtomicBoolean(false)
    private lateinit var id: String


    private val position = AtomicReference<Point>()
    private val randomDest = AtomicReference<Point>()
    private val dead = AtomicBoolean(false)
    private val paused = AtomicBoolean(false)
    private val targetWeapon = AtomicReference<Noise>()

    @Inject
    lateinit var vertx:Vertx

    lateinit var vertxScheduler: VertxScheduler

    @Inject
    @Channel("hero-making-noise")
    lateinit var heroNoiseEmitter: Emitter<JsonObject>

    @Inject
    @Channel("display")
    lateinit var displayEmitter: Emitter<JsonObject>

    @Inject
    @Channel("load-catapult")
    @OnOverflow(OnOverflow.Strategy.BUFFER)
    lateinit var loadCatapultEmitter: Emitter<JsonObject>

    fun onStart(@Observes e: StartupEvent) {
        vertxScheduler = VertxScheduler(vertx)
        reset()
        LOG.info("$id has joined the game (${position.get().x()}, ${position.get().y()})")
        vertxScheduler.schedule(200, this::scheduled)
        initialized.set(true)
    }

    fun onShutdown(@Observes e: ShutdownEvent) {
        vertxScheduler.cancel()
    }

    fun scheduled() {
        if (!paused.get() && !dead.get()) {
            makeNoise()
            if (targetWeapon.get() != null) {
                if (isOnWeapon()) {
                    loadCatapult()
                } else {
                    walk()
                }
            } else {
                walkRandom()
            }
        }
        display()
    }

    private fun loadCatapult() {
        val t = targetWeapon.get()
        if (t != null) {
            kotlin.runCatching {
                for (i in 0..30) {
                    loadCatapultEmitter.send(JsonObject().put("id", t.id).put("val", i))
                }
            }.onFailure {
                LOG.warning("Error while loading catapult: ${it.message}")
            }
        }
    }

    private fun walk() {
        position.set(Players.walk(
                rnd = RND,
                pos = position.get(),
                dest = targetWeapon.get().toPoint(),
                accuracy = accuracy.get(),
                speed = speed.get(),
                delta = DELTA_MS.toDouble() / 1000
        ))

        LOG.finest("$id at ${position.get()} walking toward ${targetWeapon.get()}")
    }

    private fun walkRandom() {
        val positions = Players.walkRandom(
                rnd = RND,
                pos = position.get(),
                dest = randomDest.get(),
                accuracy = accuracy.get(),
                speed = speed.get(),
                delta = DELTA_MS.toDouble() / 1000
        )
        position.set(positions.first)
        randomDest.set(positions.second)

        LOG.finest("$id at ${position.get()} walking randomly")
    }

    private fun isOnWeapon(): Boolean {
        val t = targetWeapon.get()
        if (t != null) {
            return Circle.fromCenter(position.get(), 20.0).contains(t.toPoint())
        }
        return false
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
        if(!initialized.get()) {
            return
        }
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
        position.set(GameObjects.startingPoint(RND, Areas.spawnHeroesArea, X, Y))
        targetWeapon.set(null)
        randomDest.set(null)
    }

    @Incoming("villain-making-noise")
    fun villainMakingNoise(o: JsonObject) {
        if(!initialized.get()) {
            return
        }
        val noise = o.mapTo(Noise::class.java)
        LOG.finest("$id received villain noise: $noise")
    }

    @Incoming("weapon-making-noise")
    fun weaponMakingNoise(o: JsonObject) {
        if(!initialized.get()) {
            return
        }
        val noise = o.mapTo(Noise::class.java)
        LOG.finest("$id received weapon noise: $noise")
        val currentTarget = targetWeapon.get()
        if (currentTarget == null) {
            // New target
            targetWeapon.set(noise)
        } else if (!isOnWeapon()) {
            val pos = position.get()
            val currentStrength = currentTarget.strength(pos)
            val newStrength = noise.strength(pos)
            if (newStrength > currentStrength) {
                targetWeapon.set(noise)
            }
        }
    }

    @Incoming("kill-around")
    fun onKillAround(o: JsonObject) {
        if(!initialized.get()) {
            return
        }
        if (dead.get()) {
            return
        }
        val zone = o.mapTo(Circle::class.java)
        if (zone.contains(position.get())) {
            LOG.info("Uuuuuhhggg!!!! (Today, $id has died)")
            dead.set(true)
        }
    }

    @Incoming("kill-single")
    fun onKillSingle(o: JsonObject) {
        if(!initialized.get()) {
            return
        }
        if (dead.get()) {
            return
        }
        if (id == o.getString("id")) {
            LOG.info("Uuuuuhhggg!!!! (Today, $id has died)")
            dead.set(true)
        }
    }

    private fun makeNoise() {
        heroNoiseEmitter.send(JsonObject.mapFrom(Noise.fromPoint(id, position.get())))
    }
}
