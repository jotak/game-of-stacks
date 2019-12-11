package demo.gos.gm

import demo.gos.common.*
import demo.gos.common.ElementStatus.DEAD
import demo.gos.common.ElementType.HERO
import demo.gos.common.ElementType.WEAPON
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import kotlin.streams.toList

@Path("/gm")
class GameManager {

    companion object {
        val ELEMENTS = listOf(
                Element("Aria Stark", HERO, 500.0, 400.0, ElementStatus.ALIVE),
                Element("Ned Stark", HERO, 500.0, 200.0, ElementStatus.ALIVE),
                Element("John Snow", HERO, 500.0, 100.0, ElementStatus.ALIVE),
                Element("Daenerys Targaryen", HERO, 400.0, 400.0, ElementStatus.ALIVE)
        )
        val AREAS = listOf(
                Area(Areas.SPAWN_VILLAINS, -100.0, 0.0, 100.0, 600.0),
                Area(Areas.SPAWN_HEROES, 400.0, 100.0, 200.0, 400.0),
                Area(Areas.SPAWN_WEAPONS, 300.0, 200.0, 100.0, 200.0)
        )
    }

    val elementsMap = ConcurrentHashMap(ELEMENTS.associateBy { it.id }.toMutableMap())
    val weaponsOwnerMap = ConcurrentHashMap<String, String>()
    val areasMap = AREAS.associateBy { it.name }

    @GET
    @Path("/elements")
    @Produces(MediaType.APPLICATION_JSON)
    fun listElements(@QueryParam("ids") ids: Set<String>, @QueryParam("type") type: ElementType?, @QueryParam("status") status: ElementStatus?): Collection<Element> {
        if (ids.isNotEmpty()) {
            return elementsMap
                    .filterKeys { ids.contains(it) }
                    .values
        }
        return queryElements(type, status)
    }

    @DELETE
    @Path("/elements")
    fun removeElements(@QueryParam("ids") ids: Set<String>? = null) {
        if (ids == null) {
            return elementsMap.clear()
        }
        ids.forEach { elementsMap.remove(it) }
    }

    @GET
    @Path("/element/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getElement(@PathParam("id") id: String): Element? {
        return elementsMap[id]
    }

    @PATCH
    @Path("/action")
    @Consumes(MediaType.APPLICATION_JSON)
    fun action(action: Action) {
        when (action) {
            is KillAction -> {
                killAction(action)
            }
            is MoveAction -> {
                moveAction(action)
            }
            else -> {
                throw IllegalStateException("Invalid patch type")
            }
        }
    }

    fun moveAction(action: MoveAction) {
        if (elementsMap[action.id] == null) {
            // element does not exist
            return
        }
        if (elementsMap[action.id]?.status == DEAD) {
            // element is dead
            return
        }
        elementsMap.computeIfPresent(action.id)
        { elId: String, element: Element ->
            element.copy(x = action.x, y = action.y)
        }
    }

    fun killAction(action: KillAction) {
        if (elementsMap[action.targetId] == null) {
            // target does not exist
            return
        }
        if (elementsMap[action.killerId] == null) {
            // killer does not exist
            return
        }
        if (elementsMap[action.targetId]?.status == DEAD) {
            // target is already dead
            return
        }
        if (elementsMap[action.killerId]?.status == DEAD) {
            // killer is already dead
            return
        }
        if (action.kamikaze) {
            elementsMap.computeIfPresent(action.killerId) { elId: String, element: Element ->
                return@computeIfPresent element.copy(status = DEAD)
            }
        }
        elementsMap.computeIfPresent(action.targetId) { elId: String, element: Element ->
            element.copy(status = DEAD)
        }
    }


    @PATCH
    @Path("/action/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    fun actionBacth(actions: Collection<Action>) {
        actions.map { action(it) }
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
    @Path("/weapon/{id}/arm/{hero}")
    @Produces(MediaType.APPLICATION_JSON)
    fun lockWeapon(@PathParam("id") id: String, @PathParam("hero") hero: String): Boolean {
        checkNotNull(elementsMap[id])
        checkNotNull(elementsMap[hero])
        check(elementsMap[id]!!.type != WEAPON) { "This element $id is not a weapon" }
        check(elementsMap[id]!!.status != DEAD) { "This element $id is dead" }
        check(elementsMap[hero]!!.type != HERO) { "This element $hero is not a hero" }
        check(elementsMap[hero]!!.status != DEAD) { "This element $hero is dead" }
        return weaponsOwnerMap.computeIfAbsent(id) {
            return@computeIfAbsent hero
        } == hero
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
                .filter { status == null || it.status == status }
                .filter { type == null || it.type == type }
                .toList()

    }

    @GET
    @Path("/areas")
    @Produces(MediaType.APPLICATION_JSON)
    fun listAreas(): Collection<Area> {
        return AREAS
    }

    @GET
    @Path("/areas/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAreas(@PathParam("name") name: String): Area? {
        return areasMap[name]
    }
}