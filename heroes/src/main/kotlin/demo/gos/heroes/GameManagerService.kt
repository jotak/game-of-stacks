package demo.gos.heroes

import demo.gos.common.Element
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/gm")
@RegisterRestClient
interface GameManagerService {


    @POST
    @Path("/weapon/{id}/arm/{hero}")
    @Produces(MediaType.APPLICATION_JSON)
    fun lockWeapon(@PathParam("id") id: String, @PathParam("hero") hero: String): Boolean

    @POST
    @Path("/element/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createElementBatch(elements: List<Element>): List<Element>

}