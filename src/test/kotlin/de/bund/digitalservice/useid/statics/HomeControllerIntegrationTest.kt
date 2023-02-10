package de.bund.digitalservice.useid.statics

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class HomeControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) {

    @Test
    fun `should redirect to public facing website`() {
        val res = webTestClient
            .get()
            .uri("/")
            .exchange()

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
