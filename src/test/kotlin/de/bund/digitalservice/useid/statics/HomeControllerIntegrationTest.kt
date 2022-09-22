package de.bund.digitalservice.useid.statics

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient

class HomeControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) : PostgresTestcontainerIntegrationTest() {

    @Test
    fun `should redirect to public facing website`() {
        webTestClient
            .get()
            .uri("/")
            .exchange()
            .expectStatus()
            .isSeeOther
            .expectHeader()
            .location("https://digitalservice.bund.de")
    }
}
