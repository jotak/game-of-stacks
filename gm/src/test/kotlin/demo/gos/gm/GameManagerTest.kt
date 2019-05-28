package demo.gos.gm

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

@QuarkusTest
open class GameManagerTest {

    @Test
    fun testHelloEndpoint() {
        given()
          .`when`().get("/gm")
          .then()
             .statusCode(200)
             .body(`is`("hello"))
    }

}