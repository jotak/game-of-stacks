package demo.gos.gm

import demo.gos.common.Element
import demo.gos.common.ElementType
import demo.gos.common.KillAction
import demo.gos.common.MoveAction
import demo.gos.common.ElementStatus.ALIVE
import demo.gos.common.ElementStatus.DEAD
import demo.gos.common.ElementType.VILLAIN
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import javax.inject.Inject

@QuarkusTest
open class GameManagerTest {

    companion object {
        val ELEMENTS = listOf(
                Element("Aria Stark", ElementType.HERO, 500.0, 400.0, ALIVE),
                Element("Ned Stark", ElementType.HERO, 500.0, 200.0, ALIVE),
                Element("John Snow", ElementType.HERO, 500.0, 100.0, ALIVE),
                Element("Daenerys Targaryen", ElementType.HERO, 400.0, 400.0, ALIVE)
        )
    }

    @Inject
    lateinit var gm: GameManager

    @Test
    fun testHelloEndpoint() {
        given()
          .`when`().get("/gm/elements")
          .then()
             .statusCode(200)
    }

    @Test
    fun testKillKamikazeAction() {
        gm.removeElements()
        gm.elementsMap.putAll(ELEMENTS.associateBy { it.id }.toMutableMap())
        val ww1 = Element("WW1", VILLAIN, 0.0, 0.0, ALIVE)
        gm.createElement(ww1)
        val patch = KillAction(killerId = ww1.id, targetId = "Aria Stark", kamikaze = true)
        given().body(patch)
                .contentType(ContentType.JSON)
                .`when`()
                .patch("/gm/action")
                .then()
                .log().ifError()
                .statusCode(204)

        Assertions.assertThat(gm.getElement("Aria Stark")!!.status).isEqualTo(DEAD)
        Assertions.assertThat(gm.getElement("WW1")!!.status).isEqualTo(DEAD)
    }

    @Test
    fun testKillAction() {
        gm.removeElements()
        gm.elementsMap.putAll(ELEMENTS.associateBy { it.id }.toMutableMap())
        val ww1 = Element("WW1", VILLAIN, 0.0, 0.0, ALIVE)
        gm.createElement(ww1)
        val patch = KillAction(killerId = "Aria Stark", targetId = ww1.id, kamikaze = false)
        given().body(patch)
                .contentType(ContentType.JSON)
                .`when`()
                .patch("/gm/action")
                .then()
                .log().ifError()
                .statusCode(204)

        Assertions.assertThat(gm.getElement("Aria Stark")!!.status).isEqualTo(ALIVE)
        Assertions.assertThat(gm.getElement("WW1")!!.status).isEqualTo(DEAD)
    }

    @Test
    fun testKillActionWhenDead() {
        gm.removeElements()
        gm.elementsMap.putAll(ELEMENTS.associateBy { it.id }.toMutableMap())
        val ww1 = Element("WW1", VILLAIN, 0.0, 0.0, DEAD)
        gm.createElement(ww1)
        val patch = KillAction(killerId = ww1.id, targetId = "Aria Stark", kamikaze = true)
        given().body(patch)
                .contentType(ContentType.JSON)
                .`when`()
                .patch("/gm/action")
                .then()
                .log().ifError()
                .statusCode(204)

        Assertions.assertThat(gm.getElement("Aria Stark")!!.status).isEqualTo(ALIVE)
        Assertions.assertThat(gm.getElement("WW1")!!.status).isEqualTo(DEAD)
    }

    @Test
    fun testMoveAction() {
        gm.removeElements()
        gm.elementsMap.putAll(ELEMENTS.associateBy { it.id }.toMutableMap())
        val ww1 = Element("WW1", VILLAIN, 0.0, 0.0, ALIVE)
        gm.createElement(ww1)
        val patch = MoveAction(ww1.id, 1.0, 2.0)
        given().body(patch)
                .contentType(ContentType.JSON)
                .`when`()
                .patch("/gm/action")
                .then()
                .log().ifError()
                .statusCode(204)
        Assertions.assertThat(gm.getElement("WW1")!!.x).isEqualTo(1.0)
        Assertions.assertThat(gm.getElement("WW1")!!.y).isEqualTo(2.0)
    }

    @Test
    fun testMoveActionWhenDead() {
        gm.removeElements()
        gm.elementsMap.putAll(ELEMENTS.associateBy { it.id }.toMutableMap())
        val ww1 = Element("WW1", VILLAIN, 0.0, 0.0, DEAD)
        gm.createElement(ww1)
        val patch = MoveAction(ww1.id, 1.0, 2.0)
        given().body(patch)
                .contentType(ContentType.JSON)
                .`when`()
                .patch("/gm/action")
                .then()
                .log().ifError()
                .statusCode(204)
        Assertions.assertThat(gm.getElement("WW1")!!.x).isEqualTo(0.0)
        Assertions.assertThat(gm.getElement("WW1")!!.y).isEqualTo(0.0)
    }

}