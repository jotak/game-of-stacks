package demo.gos.gm

import demo.gos.common.ElementStatus.ALIVE
import demo.gos.common.ElementType.*
import java.util.logging.Logger
import javax.inject.Inject
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.Provider

@Provider
class LoggingFilter : ContainerRequestFilter {

    companion object {
        val LOG: Logger = Logger.getLogger(LoggingFilter::class.java.name)
    }

    @Inject
    lateinit var gameManager: GameManager

    @Context
    lateinit var info: UriInfo

    override fun filter(context: ContainerRequestContext) {
        LOG.finest { "Received ${context.method} on ${info.path}" }
        val villains = gameManager.queryElements(VILLAIN)
        LOG.finer { "${villains.filter { it.status == ALIVE }.size} villains left" }
        LOG.finest { villains.joinToString("\n")  { "${it.id} (${it.x},${it.y}) is ${it.status}" } }
        val heroes = gameManager.queryElements(HERO)
        LOG.finer { "${heroes.filter { it.status == ALIVE }.size} heroes left" }
        LOG.finest { heroes.joinToString("\n")  { "${it.id} (${it.x},${it.y}) is ${it.status}" } }

    }
}