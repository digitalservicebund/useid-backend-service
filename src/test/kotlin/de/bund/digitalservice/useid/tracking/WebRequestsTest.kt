package de.bund.digitalservice.useid.tracking

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(value = [OutputCaptureExtension::class, SpringExtension::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebRequestsTest : PostgresTestcontainerIntegrationTest() {

    @Autowired
    private lateinit var webRequests: WebRequests

    @Test
    fun `POST method should return 200 when given an url that returns 200 as status code`(output: CapturedOutput) {
        val response = webRequests
            .POST("https://httpstat.us/200")
            .block()
        assertEquals(HttpStatus.OK, response?.statusCode)
    }

    @Test
    fun `POST method should return 500 when given an url that returns 404 as status code`(output: CapturedOutput) {
        val response = webRequests
            .POST("https://httpstat.us/404")
            .block()
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response?.statusCode)
    }

    @Test
    fun `POST method should return 500 when given an url that returns 401 as status code`(output: CapturedOutput) {
        val response = webRequests
            .POST("https://httpstat.us/401")
            .block()
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response?.statusCode)
    }
}
