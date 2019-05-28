package demo.gos.gm

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/gm")
class GameManager {

    val elements = listOf(
            Element("whitewalker1", ElementType.BAD_GUY, 0, 0, ElementStatus.ALIVE),
            Element("whitewalker2", ElementType.BAD_GUY, 0, 0, ElementStatus.ALIVE),
            Element("aria", ElementType.GOOD_GUY, 0, 0, ElementStatus.ALIVE)
            )

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    fun all(): List<Element> {
        return elements
    }
}