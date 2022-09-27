package de.bund.digitalservice.useid.statics

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class WellKnownControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) {

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
