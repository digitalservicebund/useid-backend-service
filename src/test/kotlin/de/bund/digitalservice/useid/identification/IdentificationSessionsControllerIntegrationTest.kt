package de.bund.digitalservice.useid.identification

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.eidservice.EidService
import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.UUID

private const val AUTHORIZATION_HEADER = "Bearer some-api-key"
private const val REFRESH_ADDRESS = "some-refresh-address"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class IdentificationSessionsControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) {
    val attributes = listOf("DG1", "DG2")

    @Autowired
    private lateinit var identificationSessionService: IdentificationSessionService

    @Autowired
    private lateinit var applicationProperties: ApplicationProperties

    @MockkBean
    private lateinit var eidService: EidService

    @Test
    fun `start session endpoint returns TCTokenUrl`() {
        var tcTokenURL = ""

        sendCreateSessionRequest()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        val session = retrieveIdentificationSession(tcTokenURL)
        assertThat(session.eIDSessionId, nullValue())
        assertThat(session.useIDSessionId, notNullValue())
        assertThat(session.requestAttributes, `is`(attributes))
        assertThat(session.refreshAddress, `is`(REFRESH_ADDRESS))

        val expectedTcTokenURL = "${applicationProperties.baseUrl}/api/v1/identification/sessions/${session.useIDSessionId}/tc-token"
        assertEquals(expectedTcTokenURL, tcTokenURL)
    }

    @Test
    fun `start session endpoint returns 401 when no authorization header was passed`() {
        sendGETRequest(IDENTIFICATION_SESSIONS_BASE_PATH).exchange().expectStatus().isUnauthorized
    }

    @Test
    fun `tcToken endpoint returns valid tc-token and sets correct eIdSessionId in IdentificationSession`() {
        val mockTCToken = mockk<TCTokenType>()
        val eIdSessionId = UUID.randomUUID()
        every { mockTCToken.refreshAddress } returns "https://www.foobar.com?sessionId=$eIdSessionId"
        every { eidService.getTcToken(any()) } returns mockTCToken

        var tcTokenURL = ""
        sendCreateSessionRequest()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        sendGETRequest(extractRelativePathFromURL(tcTokenURL))
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_XML)
            .expectBody().xpath("TCTokenType").exists()

        val session = retrieveIdentificationSession(tcTokenURL)
        assertEquals(eIdSessionId, session.eIDSessionId)
    }

    @Test
    fun `tcToken endpoint returns 400 when passed an invalid UUID as useIdSessionID`() {
        val invalidId = "IamInvalid"
        sendGETRequest("/api/v1/identification/sessions/$invalidId/tc-token").exchange().expectStatus().isBadRequest
    }

    @Test
    fun `tcToken endpoint returns 404 when passed a unknown UUID as useIdSessionID`() {
        val unknownId = UUID.randomUUID()
        sendGETRequest("/api/v1/identification/sessions/$unknownId/tc-token").exchange().expectStatus().isNotFound
    }

    @Test
    fun `tcToken endpoint returns 500 when error is thrown`() {
        every { eidService.getTcToken(any()) } throws Error("internal server error")

        var tcTokenURL = ""
        sendCreateSessionRequest()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        sendGETRequest(extractRelativePathFromURL(tcTokenURL)).exchange().expectStatus().is5xxServerError
    }

    @Test
    fun `identity data endpoint returns 400 when passed an invalid string instead of UUID`() {
        sendIdentityRequest("IamInvalid")
            .expectStatus().isBadRequest
    }

    @Test
    fun `identity data endpoint returns 401 when no authorization header was passed`() {
        sendGETRequest("/api/v1/identification/sessions/${UUID.randomUUID()}").exchange().expectStatus().isUnauthorized
    }

    @Test
    fun `identity data endpoint returns 404 when passed a random UUID`() {
        sendIdentityRequest(UUID.randomUUID().toString())
            .expectStatus().isNotFound
    }

    private fun sendIdentityRequest(eIdSessionId: String) =
        sendGETRequest("/api/v1/identification/sessions/$eIdSessionId")
            .headers { setAuthorizationHeader(it) }
            .exchange()

    private fun extractRelativePathFromURL(url: String) = URI.create(url).rawPath

    private fun sendGETRequest(uri: String) = webTestClient
        .get()
        .uri(uri)

    private fun sendCreateSessionRequest() = webTestClient
        .post()
        .uri("/api/v1/identification/sessions")
        .headers { setAuthorizationHeader(it) }
        // TODO: REMOVE ATTRIBUTES WHEN TICKET USEID-299 IS FINISHED
        .body(BodyInserters.fromValue(CreateIdentitySessionRequest(attributes)))
        .exchange()

    private fun retrieveIdentificationSession(tcTokenURL: String): IdentificationSession {
        val useIDSessionId = extractUseIdSessionIdFromTcTokenUrl(tcTokenURL)
        return identificationSessionService.findByUseIDSessionId(useIDSessionId).block()!!
    }

    private fun extractUseIdSessionIdFromTcTokenUrl(tcTokenURL: String): UUID {
        val pathSegments = UriComponentsBuilder
            .fromHttpUrl(tcTokenURL)
            .encode().build().pathSegments
        val useIdSessionId = pathSegments[pathSegments.size - 2]
        return UUID.fromString(useIdSessionId)!!
    }

    private fun setAuthorizationHeader(headers: HttpHeaders) {
        headers.set(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)
    }
}
