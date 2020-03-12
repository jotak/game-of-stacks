package demo.gos.hero

import demo.gos.common.Fire
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.core.MediaType

@RegisterRestClient(configKey ="arrow-api")
@Path("arrow")
interface Arrow {

    @Path("/fire")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    fun fire(fire: Fire)
}
