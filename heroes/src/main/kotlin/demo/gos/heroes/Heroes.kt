package demo.gos.heroes

import demo.gos.common.Commons
import demo.gos.common.Element
import demo.gos.common.ElementStatus
import demo.gos.common.ElementType
import io.quarkus.runtime.StartupEvent
import io.vertx.core.Vertx
import org.eclipse.microprofile.rest.client.RestClientBuilder
import java.net.URI
import java.util.logging.Logger
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Heroes {
    companion object {
        val LOG: Logger = Logger.getLogger(Heroes::class.java.name)
        val HEROES = listOf(
                Element("Aria Stark", ElementType.HERO, 500.0, 400.0, ElementStatus.ALIVE),
                Element("Ned Stark", ElementType.HERO, 500.0, 200.0, ElementStatus.ALIVE),
                Element("John Snow", ElementType.HERO, 500.0, 100.0, ElementStatus.ALIVE),
                Element("Daenerys Targaryen", ElementType.HERO, 400.0, 400.0, ElementStatus.ALIVE)
        )
    }

    @Inject
    lateinit var vertx: Vertx

    val gm: GameManagerService = RestClientBuilder.newBuilder()
            .baseUri(URI.create(Commons.BATTLEFIELD_URI)).build(GameManagerService::class.java)


    fun onStart(@Observes e: StartupEvent) {
        LOG.info("Started")

        vertx.eventBus().consumer<Any>("start") {
            LOG.info("Received start event")
            //gm.createElementBatch(HEROES)
        }

        vertx.eventBus().consumer<Any>("stop") {
            LOG.info("Received stop event")
        }

    }

}