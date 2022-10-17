package de.bund.digitalservice.useid.widget

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WidgetControllerTest(@Autowired val webTestClient: WebTestClient) : PostgresTestcontainerIntegrationTest() {

    @Test
    fun `INCOMPATIBLE_PAGE should return 200`() {
        webTestClient
            .get()
            .uri("/$INCOMPATIBLE_PAGE")
            .exchange()
            .expectStatus().isOk
            .expectBody()
    }

    @Test
    fun `FALLBACK_PAGE should return 200`() {
        webTestClient
            .get()
            .uri("/$FALLBACK_PAGE")
            .exchange()
            .expectStatus().isOk
            .expectBody()
    }
}
