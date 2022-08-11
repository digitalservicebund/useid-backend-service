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
    fun `starting session returns TCTokenUrl if the request is made with a correct payload`() {
        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/identification/sessions"))
            .headers { setAuthorizationHeader(it) }
            .body(BodyInserters.fromValue(CreateIdentitySessionRequest(attributes)))
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.tcTokenUrl").exists()
    }

    @Test
    fun `getting identity data returns with 200 and data attributes if the session id is valid and found`() {
    /*  var mockTCTokenUrl = ""

        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/identification/sessions"))
            .headers { setAuthorizationHeader(it) }
            .body(BodyInserters.fromValue(CreateIdentitySessionRequest(attributes)))
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.tcTokenUrl").value<String> { tcTokenUrl ->
                *//**
         * Store tcTokenUrl temporarily in mockTCTokenUrl so that the next request can call it
         *//*
                mockTCTokenUrl = URLDecoder.decode(tcTokenUrl, Utils.ENCODING)
            }

         webTestClient
            .get()
            .uri(URI.create(mockTCTokenUrl))
            .headers { setAuthorizationHeader(it) }
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_XML)
            .expectBody()
            .xpath("TCTokenType").exists()
            .xpath("TCTokenType/ServerAddress").exists()
            .xpath("TCTokenType/RefreshAddress").exists()
            .xpath("TCTokenType/RefreshAddress[contains(text(), 'sessionId=')]").exists()
        */
        // implement regex search for UUID -> example: https://regex101.com/r/17Gvse/1
        // .xpath("TCTokenType/RefreshAddress[contains(text(), '/^(.*?)sessionId=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}.*/')]").exists()
    }

    @Test
    fun `getting identity data fails with 404 if the session id cannot be found`() {
        webTestClient
            .get()
            .uri(URI.create("http://localhost:$port/api/v1/identification/sessions/4793d3d3-a40e-4445-b344-189fe88f9219"))
            .headers { setAuthorizationHeader(it) }
            .exchange()
            .expectStatus()
            .isNotFound
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.status").isEqualTo("404")
            .jsonPath("$.message").isEqualTo("Could not find session.")
    }

    // TCToken
    @Test
    fun `getting TCToken returns with 500 if the UseIDSessionId of the tc token is invalid`() {
        webTestClient
            .get()
            .uri(URI.create("http://localhost:$port/api/v1/identification/sessions/tc-token/4793d3d3-a40e-4445-b344-189fe88f9219"))
            .headers { setAuthorizationHeader(it) }
            .exchange()
            .expectStatus()
            .is5xxServerError
    }

    private fun setAuthorizationHeader(headers: HttpHeaders) {
        headers.set(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)
    }
}
