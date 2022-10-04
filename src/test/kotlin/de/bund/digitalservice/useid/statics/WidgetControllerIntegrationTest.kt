package de.bund.digitalservice.useid.statics

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class WidgetControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) {

    @Test
    fun `widget endpoint should disable X-Frame-Options`() {
        webTestClient
            .get()
            .uri("/widget")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .doesNotExist("X-Frame-Options")
    }

    @Test
    fun `widget endpoint should deliver correct Content-Security-Protocol when the request URL is valid`() {
        webTestClient
            .get()
            .uri("/widget")
            .header(HttpHeaders.HOST, "foo.bar")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .valueEquals(
                "Content-Security-Policy",
                "some default value;frame-ancestors 'self' foo.bar;"
            )
    }

    @Test
    fun `widget endpoint should deliver correct Content-Security-Protocol when the request URL is invalid`() {
        webTestClient
            .get()
            .uri("/widget")
            .header(HttpHeaders.HOST, "not-allowed.com")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .valueEquals(
                "Content-Security-Policy",
                "some default value;frame-ancestors 'self';"
            )
    }
}
