package de.bund.digitalservice.useid.home

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HomeControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) {

    @Test
    fun `should redirect to public facing website`() {
        webTestClient
            .get()
            .uri("/")
            .exchange()
            .expectStatus()
            .is3xxRedirection
            .expectHeader()
            .location("https://digitalservice.bund.de")
    }
}
