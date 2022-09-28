package de.bund.digitalservice.useid.statics

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WidgetControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) : PostgresTestcontainerIntegrationTest() {

    @Test
    fun `should disable X-Frame-Options`() {
        webTestClient
            .get()
            .uri("/widget")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .doesNotExist("X-Frame-Options")
    }
}
