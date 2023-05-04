package de.bund.digitalservice.useid.identification

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

private const val E_SERVICE_HEALTH_PATH = "api/v1/eidservice/health"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class EidServiceContollerIntegrationTest(@Autowired val webTestClient: WebTestClient) {

    @Test
    fun `health endpoint returns 200 when no authorization header was passed`() {
        webTestClient.sendGETRequest(E_SERVICE_HEALTH_PATH).exchange().expectStatus().isOk
    }
}
