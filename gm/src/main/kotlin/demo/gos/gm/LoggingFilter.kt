package demo.gos.gm

import demo.gos.gm.ElementType.*
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
        LOG.finest { "${gameManager.getElementsByType(VILLAIN).size} villains left" }
        gameManager.getElementsByType(HERO).forEach {
            LOG.finest { "${it.id} (${it.x},${it.y}) is ${it.status}" }
        }

    }
}