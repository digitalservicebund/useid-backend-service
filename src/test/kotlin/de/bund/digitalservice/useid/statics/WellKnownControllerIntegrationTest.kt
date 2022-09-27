package de.bund.digitalservice.useid.statics

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient

class WellKnownControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) : PostgresTestcontainerIntegrationTest() {

    @Test
    fun `iOS deeplink endpoint returns JSON file`() {
        webTestClient
            .get()
            .uri("/.well-known/apple-app-site-association")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.applinks.details[0].appIDs").exists()
            .jsonPath("$.applinks.details[0].components").exists()
    }
}
