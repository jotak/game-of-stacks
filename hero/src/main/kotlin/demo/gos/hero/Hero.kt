package demo.gos.hero

import demo.gos.common.*
import demo.gos.common.maths.Point
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import io.reactivex.Flowable
import io.smallrye.reactive.messaging.annotations.Channel
import io.smallrye.reactive.messaging.annotations.Emitter
import io.smallrye.reactive.messaging.annotations.OnOverflow
import io.vertx.core.json.JsonObject
import org.apache.commons.lang3.RandomStringUtils
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.reactive.messaging.Incoming
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Logger
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.concurrent.schedule
import kotlin.concurrent.scheduleAtFixedRate

@Singleton
class Hero {
    companion object {
        val LOG: Logger = Logger.getLogger(Hero::class.java.name)
        const val DELTA_MS = 1000L
        val HEROES = listOf(
                "aria-stark", "bran-stark", "catelyn-stark", "cersei-lannister", "daenerys-targaryen", "davos-seaworth", "gendry", "jaime-lannister", "jeor-mormont", "jon-snow", "kal-drogo", "lady-brynne", "little-finger", "margeary-tyrell", "melisandre", "ned-stark", "prince-joffrey", "rickon-stark", "robb-stark", "robert-barratheon", "sansa-stark", "stannis-barratheon", "the-dog", "theon-greyjoy", "tyrion-lannister", "tywin-lannister", "varys", "ygritte"
        )
    }

    val timer = Timer()
    val RND = SecureRandom()

    @ConfigProperty(name = "X")
    lateinit var X: Optional<Double>

    @ConfigProperty(name = "runtime")
    lateinit var runtime: Optional<String>

    @ConfigProperty(name = "Y")
    lateinit var Y: Optional<Double>

    @ConfigProperty(name = "name")
    lateinit var configName: Optional<String>

    @ConfigProperty(name = "use-bow", defaultValue = "false")
    lateinit var useBow: Provider<Boolean>

    @ConfigProperty(name = "accuracy", defaultValue = "0.7")
    lateinit var accuracy: Provider<Double>

    @ConfigProperty(name = "speed", defaultValue = "90.0")
    lateinit var speed: Provider<Double>


    private val id = AtomicReference<String>()
    private val name = AtomicReference<String>()

    private val randomDest = AtomicReference<Point>()
    private val dead = AtomicBoolean(false)

    private val targetWeapon = AtomicReference<Noise>()

    val initialized = AtomicBoolean(false)
    val position = AtomicReference<Point>()
    val paused = AtomicBoolean(false)

    @Inject
    @Channel("hero-making-noise")
    lateinit var heroNoiseEmitter: Emitter<JsonObject>

    @Inject
    @Channel("display")
    lateinit var displayEmitter: Emitter<JsonObject>

    @Inject
    @Channel("load-weapon")
    @OnOverflow(OnOverflow.Strategy.BUFFER)
    lateinit var loadWeaponEmitter: Emitter<JsonObject>

    @Inject
    @Channel("weapon-making-noise")
    lateinit var weaponMakingNoiseFlowable: Flowable<JsonObject>

    fun onStart(@Observes e: StartupEvent) {
        reset()
        LOG.info("$id has joined the game (${position.get().x()}, ${position.get().y()}) ${if(useBow.get()) "with a bow" else ""}")
        initialized.set(true)
        timer.scheduleAtFixedRate(0L, DELTA_MS) {
            scheduled()
        }
        if (!useBow.get()) {
            weaponMakingNoiseFlowable
                    .takeUntil { targetWeapon.get() != null }
                    .subscribe { weaponMakingNoise(it) }
        } else {
            timer.schedule(DELTA_MS) {
                // Move to the weapon area
                val pos = position.get()
                position.set(GameObjects.startingPoint(RND, Areas.spawnWeaponArea, null, pos.y()))
            }
        }
    }

    fun onShutdown(@Observes e: ShutdownEvent) {
        timer.cancel()
    }

    fun scheduled() {
        if (!paused.get() && !dead.get()) {
            makeNoise()
            if (targetWeapon.get() != null) {
                if (isOnWeapon()) {
                    loadWeapon()
                } else {
                    walk()
                }
            } else if(!useBow.get()) {
                walkRandom()
            }
        }
        display()
    }

    private fun loadWeapon() {
        val t = targetWeapon.get()
        if (t != null) {
            kotlin.runCatching {
                loadWeaponEmitter.send(JsonObject().put("id", t.id).put("val", 1))
            }.onFailure {
                LOG.warning("Error while loading weapon: ${it.message}")
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
        val sprite = if (dead.get()) "rip" else name.get()
        val data = DisplayData(
                id = id.get(),
                x = position.get().x(),
                y = position.get().y(),
                sprite = sprite,
                label = id.get()
        )
        val json = JsonObject.mapFrom(data)
        displayEmitter.send(json)
    }

    @Incoming("game")
    fun game(o: JsonObject) {
        if (!initialized.get()) {
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
        name.set(configName.orElse(HEROES[RND.nextInt(HEROES.size)]))
        id.set("${name.get()}-${runtime.orElse("")}-${RandomStringUtils.random(3, 0, 0, true, true, null, RND)}")
        paused.set(false)
        dead.set(false)
        position.set(GameObjects.startingPoint(RND, Areas.spawnHeroesArea, X.orElse(null), Y.orElse(null)))
        targetWeapon.set(null)
        randomDest.set(null)
    }

    fun weaponMakingNoise(o: JsonObject) {
        if (!initialized.get()) {
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
        if (!initialized.get()) {
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
        if (!initialized.get()) {
            return
        }
        if (dead.get()) {
            return
        }
        if (id.get() == o.getString("id")) {
            LOG.info("Uuuuuhhggg!!!! (Today, $id has died)")
            dead.set(true)
        }
    }

    private fun makeNoise() {
        heroNoiseEmitter.send(JsonObject.mapFrom(Noise.fromPoint(id.get(), position.get())))
    }
}
