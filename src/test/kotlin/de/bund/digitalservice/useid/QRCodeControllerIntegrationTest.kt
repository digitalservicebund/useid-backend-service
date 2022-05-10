package de.bund.digitalservice.useid

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class QRCodeControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) {

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
