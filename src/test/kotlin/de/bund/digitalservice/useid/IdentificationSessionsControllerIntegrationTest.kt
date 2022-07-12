package de.bund.digitalservice.useid

import de.bund.digitalservice.useid.model.ClientRequestSession
import de.bund.digitalservice.useid.utils.IdGenerator
import io.mockk.unmockkObject
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.net.URI
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class IdentificationSessionsControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired @Value("\${local.server.port}") val port: Int
) {
    val attributes = listOf("DG1", "DG2")

    @Test
    fun `should return TCToken Url and Session Id when request is made with correct payload`() {
        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/identification/sessions"))
            .body(BodyInserters.fromValue(ClientRequestSession("https://digitalservice.bund.de", attributes)))
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.tcTokenUrl").isEqualTo("https://digitalservice.bund.de")
            .jsonPath("$.sessionId").value<String> { sessionId ->
                UUID.fromString(sessionId) is UUID
            }
    }
    @Test
    fun `should return correct identity`() {
        var mockUuid = ""

        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/identification/sessions"))
            .body(BodyInserters.fromValue(ClientRequestSession("https://digitalservice.bund.de", attributes)))
            .exchange()
            .expectBody()
            .jsonPath("$.sessionId").value<String> { sessionId ->
                /**
                 * Store sessionId temporarily in mockUuid so that the next request
                 * can include the uuid in the path variable
                 */
                mockUuid = sessionId
            }

        webTestClient
            .get()
            .uri(URI.create("http://localhost:$port/api/v1/identification/sessions/$mockUuid"))
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
            .get()
            .uri(URI.create("http://localhost:$port/api/v1/identification/sessions/1111-1111-1111-1111"))
            .exchange()
            .expectStatus()
            .isNotFound
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.status").isEqualTo("404")
            .jsonPath("$.message").isEqualTo("Error: sessionId is not found")
    }
}
