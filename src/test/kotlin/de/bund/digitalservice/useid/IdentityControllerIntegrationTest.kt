package de.bund.digitalservice.useid

import de.bund.digitalservice.useid.model.ClientRequestIdentity
import de.bund.digitalservice.useid.model.ClientRequestSession
import de.bund.digitalservice.useid.utils.IdGenerator
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class IdentityControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired @Value("\${local.server.port}") val port: Int
) {
    @Test
    fun `should return correct identity`() {
        val attributes = listOf("DG1", "DG2")
        val uuid = "my-fake-uuid"
        mockkObject(IdGenerator)
        every { IdGenerator.generateUUID() } returns uuid

        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/session"))
            .body(BodyInserters.fromValue(ClientRequestSession("https://digitalservice.bund.de", attributes)))
            .exchange()

        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/identity"))
            .body(BodyInserters.fromValue(ClientRequestIdentity("my-fake-uuid")))
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.dg1").isEqualTo("firstname")
            .jsonPath("$.dg2").isEqualTo("lastname")

        unmockkObject(IdGenerator)
    }

    @Test
    fun `should handle incorrect sessionId`() {
        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/identity"))
            .body(BodyInserters.fromValue(ClientRequestIdentity("non-existing-session-id")))
            .exchange()
            .expectStatus()
            .isNotFound
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.status").isEqualTo("404")
            .jsonPath("$.message").isEqualTo("Error: sessionId is not found")
    }

    @Test
    fun `should handle missing sessionId`() {
        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/identity"))
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.status").isEqualTo("400")
            .jsonPath("$.error").isEqualTo("Bad Request")
    }
}
