package demo.gos.gm

import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/gm")
class GameManager {

    companion object {
        val ELEMENTS = listOf(
                Element("whitewalker1", ElementType.VILLAIN, 0.0, 0.0, ElementStatus.ALIVE),
                Element("whitewalker2", ElementType.VILLAIN, 0.0, 0.0, ElementStatus.ALIVE),
                Element("aria", ElementType.HERO, 500.0, 400.0, ElementStatus.ALIVE)
        )
    }

    val elementsMap = ELEMENTS.associateBy { it.id }.toMutableMap()

    @GET
    @Path("/elements")
    @Produces(MediaType.APPLICATION_JSON)
    fun listElements(@QueryParam("ids") ids: Set<String>, @QueryParam("type") type: ElementType?): Collection<Element> {
        check(ids.isEmpty() || type == null) { "Those filters don't make sense together." }
        if (ids.isEmpty() && type === null) {
            return elementsMap.values
        }
        if (type != null) {
            return elementsMap.values.filter { it.type == type }
        }
        return elementsMap
                .filterKeys { ids!!.contains(it) }
                .values
    }

    @DELETE
    @Path("/elements")
    fun removeElements(@QueryParam("ids") ids: Set<String>?) {
        if (ids == null) {
            return elementsMap.clear()
        }
        ids.forEach { elementsMap.remove(it) }
    }

    @PATCH
    @Path("/element/:id")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun patchElement(@PathParam("id") id: String, x: Double?, y: Double?, status: ElementStatus?): Element {
        return elementsMap.computeIfPresent(id) { id: String, element: Element ->
            Element(
                    id = id,
                    x = x ?: element.x,
                    y = y ?: element.y,
                    status = status ?: element.status,
                    type = element.type
            )
        }!!
    }

    @PATCH
    @Path("/element/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun patchElementBatch(patches: Collection<ElementPatch>): Collection<Element> {
        return patches.map { patchElement(it.id, it.x, it.y, it.status) }
    }

    @POST
    @Path("/element")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createElement(element:Element): Element {
        check(elementsMap.putIfAbsent(element.id, element) == null) { "This element already exists ${element.id}" }
        return element
    }

    @POST
    @Path("/element/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createElementBatch(elements:List<Element>): List<Element> {
        return elements.map { createElement(it) }
    }
}