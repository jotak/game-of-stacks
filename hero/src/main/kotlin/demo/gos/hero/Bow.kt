package demo.gos.hero

import demo.gos.common.DisplayData
import demo.gos.common.Fire
import demo.gos.common.Noise
import demo.gos.common.maths.Point
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import io.reactivex.Flowable
import io.smallrye.reactive.messaging.annotations.Channel
import io.smallrye.reactive.messaging.annotations.Emitter
import io.vertx.core.json.JsonObject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Logger
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.concurrent.scheduleAtFixedRate


@Singleton
class Bow {
    companion object {
        val LOG: Logger = Logger.getLogger(Bow::class.java.name)
        const val DELTA_MS = 1000L
    }

    val timer = Timer()
    val RND = SecureRandom()
    val id = "BOW-Q-" + UUID.randomUUID().toString()

    @ConfigProperty(name = "accuracy", defaultValue = "0.7")
    lateinit var accuracy: Provider<Double>

    @ConfigProperty(name = "speed", defaultValue = "35.0")
    lateinit var speed: Provider<Double>

    @ConfigProperty(name = "use-bow", defaultValue = "false")
    lateinit var useBow: Provider<Boolean>

    private val target = AtomicReference<Noise>()
    private val running = AtomicBoolean(false)

    @Inject
    @Channel("display")
    lateinit var displayEmitter: Emitter<JsonObject>

    @Inject
    @Channel("villain-making-noise")
    lateinit var villainMakingNoiseFlowable: Flowable<JsonObject>

    @Inject
    lateinit var hero: Hero

    @Inject
    @field: RestClient
    lateinit var arrow: Arrow

    fun onStart(@Observes e: StartupEvent) {
        if (!useBow.get()) {
            return
        }
        target.set(null)
        timer.scheduleAtFixedRate(3 * DELTA_MS, DELTA_MS) {
            scheduled()
        }
        running.set(true)
        villainMakingNoiseFlowable
                .takeUntil { !running.get() }
                .subscribe { listenToVillains(it.mapTo(Noise::class.java)) }

    }

    fun onShutdown(@Observes e: ShutdownEvent) {
        running.set(false)
        timer.cancel()
        target.set(null)
    }

    private fun scheduled() {
        if (!hero.initialized.get() || hero.paused.get()) {
            return
        }
        display()
        fire()
    }

    private fun display() {
        val pos = getBowPosition()
        if (pos != null) {
            val data = DisplayData(
                    id = id,
                    x = pos.x(),
                    y = pos.y(),
                    sprite = "bow"
            )
            val json = JsonObject.mapFrom(data)
            displayEmitter.send(json)
        }
    }

    private fun getBowPosition() = hero.position.get().add(Point(-3.0, 0.5))

    fun fire() {
        val t = target.get()
        if(t != null) {
            arrow.fire(Fire(Noise.fromPoint(id, getBowPosition()), t))
            target.set(null)
        }
    }

    private fun listenToVillains(noise: Noise) {
        if (!hero.initialized.get() || hero.paused.get()) {
            return
        }
        target.getAndUpdate {
            if (it == null) {
                return@getAndUpdate noise
            } else {
                if (noise.id == it.id) {
                    // Update target position
                    return@getAndUpdate noise
                } else {
                    val pos = getBowPosition()
                    val currentStrength = it.strength(pos)
                    val newStrength = noise.strength(pos)
                    // 5% chances to get attention
                    if (newStrength > currentStrength && RND.nextInt(100) < 5) {
                        return@getAndUpdate noise
                    }
                    return@getAndUpdate it
                }
            }
        }
    }

}
