package de.bund.digitalservice.useid.identification

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.net.URI
import java.util.UUID

private const val AUTHORIZATION_HEADER = "Bearer some-api-key"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class IdentificationSessionsControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired @Value("\${local.server.port}")
    val port: Int
) {
    val attributes = listOf("DG1", "DG2")

    @Test
    fun `requesting identification sessions returns 401 when the request is made without basic authentication`() {
        webTestClient
            .get()
            .uri(URI.create("http://localhost:$port/api/v1/identification/sessions/"))
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `starting session returns TCToken Url and Session Id if the request is made with a correct payload`() {
        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/identification/sessions"))
            .headers { headers -> setAuthorizationHeader(headers) }
            .body(BodyInserters.fromValue(CreateIdentitySessionRequest("https://digitalservice.bund.de", attributes)))
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.tcTokenUrl").isEqualTo("https://useid.dev/12345678")
            .jsonPath("$.sessionId").value<String> { sessionId ->
                UUID.fromString(sessionId) is UUID
            }
    }

    @Test
    fun `getting identity data returns with 200 and data attributes if the session id is valid and found`() {
        var mockUuid = ""

        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/identification/sessions"))
            .headers { headers -> setAuthorizationHeader(headers) }
            .body(BodyInserters.fromValue(CreateIdentitySessionRequest("https://digitalservice.bund.de", attributes)))
            .exchange()
            .expectStatus().isOk
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
            .headers { headers -> setAuthorizationHeader(headers) }
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.dg1").isEqualTo("firstname")
            .jsonPath("$.dg2").isEqualTo("lastname")
    }

    @Test
    fun `getting identity data fails with 404 if the session id cannot be found`() {
        webTestClient
            .get()
            .uri(URI.create("http://localhost:$port/api/v1/identification/sessions/4793d3d3-a40e-4445-b344-189fe88f9219"))
            .headers { headers -> setAuthorizationHeader(headers) }
            .exchange()
            .expectStatus()
            .isNotFound
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.status").isEqualTo("404")
            .jsonPath("$.message").isEqualTo("Could not find session.")
    }

    private fun setAuthorizationHeader(headers: HttpHeaders) {
        headers.set(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)
    }
}
