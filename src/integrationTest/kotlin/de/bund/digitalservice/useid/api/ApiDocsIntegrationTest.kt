package de.bund.digitalservice.useid.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiDocsIntegrationTest(@Autowired val webTestClient: WebTestClient) {

    @Test
    fun `api docs ui endpoint returns redirects to swagger ui`() {
        webTestClient
            .get()
            .uri("/api/docs")
            .exchange()
            .expectStatus().is3xxRedirection
            .expectHeader().location("/api/swagger-ui/index.html")
    }

    @Test
    fun `swagger ui endpoint returns html page successfully`() {
        webTestClient
            .get()
            .uri("/api/swagger-ui/index.html")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.TEXT_HTML)
    }

    @Test
    fun `api docs endpoint returns json representation of api docs successfully`() {
        webTestClient
            .get()
            .uri("/api/docs.json")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
    }
}
