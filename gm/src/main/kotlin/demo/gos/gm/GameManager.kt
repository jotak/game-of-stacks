package demo.gos.gm

import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import kotlin.streams.toList

@Path("/gm")
class GameManager {

    companion object {
        val ELEMENTS = listOf(
                Element("Aria Stark", ElementType.HERO, 500.0, 400.0, ElementStatus.ALIVE),
                Element("Ned Stark", ElementType.HERO, 500.0, 200.0, ElementStatus.ALIVE),
                Element("John Snow", ElementType.HERO, 500.0, 100.0, ElementStatus.ALIVE),
                Element("Daenerys Targaryen", ElementType.HERO, 400.0, 400.0, ElementStatus.ALIVE)
        )
    }

    val elementsMap = ELEMENTS.associateBy { it.id }.toMutableMap()

    @GET
    @Path("/elements")
    @Produces(MediaType.APPLICATION_JSON)
    fun listElements(@QueryParam("ids") ids: Set<String>, @QueryParam("type") type: ElementType?, @QueryParam("status") status: ElementStatus?): Collection<Element> {
        if (!ids.isEmpty()) {
            return elementsMap
                    .filterKeys { ids.contains(it) }
                    .values
        }
        return queryElements(type, status)
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
        return elementsMap.computeIfPresent(id) { elId: String, element: Element ->
            Element(
                    id = elId,
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
    fun createElement(element: Element): Element {
        check(elementsMap.putIfAbsent(element.id, element) == null) { "This element already exists ${element.id}" }
        return element
    }

    @POST
    @Path("/element/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun createElementBatch(elements: List<Element>): List<Element> {
        return elements.map { createElement(it) }
    }

    fun queryElements(type: ElementType? = null, status: ElementStatus? = null): List<Element> {
        return elementsMap.values.stream()
                .filter { status == null || it.status == status}
                .filter { type == null || it.type == type }
                .toList()

    }
}