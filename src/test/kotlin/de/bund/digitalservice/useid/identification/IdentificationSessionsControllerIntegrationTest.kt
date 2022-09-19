package de.bund.digitalservice.useid.identification

import de.bund.bsi.eid230.GetResultResponseType
import de.bund.bsi.eid230.LevelOfAssuranceType
import de.bund.bsi.eid230.OperationsResponderType
import de.bund.bsi.eid230.PersonalDataType
import de.bund.bsi.eid230.TransactionAttestationResponseType
import de.bund.bsi.eid230.VerificationResultType
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.eidservice.EidService
import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import oasis.names.tc.dss._1_0.core.schema.Result
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
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

    @BeforeAll
    fun setup() {
        mockkConstructor(EidService::class)
    }

    @Test
    fun `start session endpoint returns TCTokenUrl`() {
        var tcTokenURL = ""

        sendCreateSessionRequest()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        val useIdSessionId = extractUseIdSessionIdFromTcTokenUrl(tcTokenURL)
        val session = retrieveIdentificationSession(useIdSessionId)
        assertThat(session.eidSessionId, nullValue())
        assertThat(session.useidSessionId, notNullValue())
        assertThat(session.requestDataGroups, `is`(attributes))
        assertThat(session.refreshAddress, `is`(REFRESH_ADDRESS))

        val expectedTcTokenURL = "${applicationProperties.baseUrl}/api/v1/identification/sessions/${session.useidSessionId}/tc-token"
        assertEquals(expectedTcTokenURL, tcTokenURL)
    }

    @Test
    fun `start session endpoint returns 401 when no authorization header was passed`() {
        sendGETRequest(IDENTIFICATION_SESSIONS_BASE_PATH).exchange().expectStatus().isUnauthorized
    }

    @Test
    fun `tcToken endpoint returns valid tc-token and sets correct eIdSessionId in IdentificationSession`() {
        var tcTokenURL = ""
        sendCreateSessionRequest()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        val eIdSessionId = UUID.randomUUID()

        mockTcToken("https://www.foobar.com?sessionId=$eIdSessionId")

        sendGETRequest(extractRelativePathFromURL(tcTokenURL))
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_XML)
            .expectBody().xpath("TCTokenType").exists()

        val useIDSessionId = extractUseIdSessionIdFromTcTokenUrl(tcTokenURL)
        val session = retrieveIdentificationSession(useIDSessionId)
        assertEquals(eIdSessionId, session.eidSessionId)
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
        every { anyConstructed<EidService>().getTcToken(any()) } throws Error("internal server error")

        var tcTokenURL = ""
        sendCreateSessionRequest()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        sendGETRequest(extractRelativePathFromURL(tcTokenURL)).exchange().expectStatus().is5xxServerError
    }

    @Test
    fun `identity data endpoint returns valid personal data and removes identification session from database`() {
        val eIdSessionId = UUID.randomUUID().toString()
        mockTcToken("https://www.foobar.com?sessionId=$eIdSessionId")

        var tcTokenURL = ""
        sendCreateSessionRequest()
            .expectStatus().isOk
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        sendGETRequest(extractRelativePathFromURL(tcTokenURL))
            .exchange()
            .expectStatus().isOk
            .expectBody().xpath("TCTokenType").exists()

        val mockResult = Result()
        mockResult.resultMajor = "http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok"
        val personalData = PersonalDataType()
        personalData.givenNames = "Ben"
        val mockGetResultResponseType = mockk<GetResultResponseType>()
        every { mockGetResultResponseType.personalData } returns personalData
        every { mockGetResultResponseType.fulfilsAgeVerification } returns VerificationResultType()
        every { mockGetResultResponseType.fulfilsPlaceVerification } returns VerificationResultType()
        every { mockGetResultResponseType.operationsAllowedByUser } returns OperationsResponderType()
        every { mockGetResultResponseType.transactionAttestationResponse } returns TransactionAttestationResponseType()
        every { mockGetResultResponseType.levelOfAssuranceResult } returns LevelOfAssuranceType.HTTP_EIDAS_EUROPA_EU_LO_A_LOW
        every { mockGetResultResponseType.result } returns mockResult
        every { anyConstructed<EidService>().getEidInformation(any()) } returns mockGetResultResponseType

        sendIdentityRequest(eIdSessionId)
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.result").value<LinkedHashMap<String, String>> {
                assertEquals(it["resultMajor"], mockResult.resultMajor)
            }
            .jsonPath("$.personalData").value<LinkedHashMap<String, String>> {
                assertEquals(it["givenNames"], personalData.givenNames)
            }

        Assertions.assertThrows(NoSuchElementException::class.java) {
            val useIDSessionId = extractUseIdSessionIdFromTcTokenUrl(tcTokenURL)
            retrieveIdentificationSession(useIDSessionId) // should throw NoSuchElementException
        }
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

    private fun mockTcToken(refreshAddress: String) {
        val mockTCToken = mockk<TCTokenType>()
        every { mockTCToken.refreshAddress } returns refreshAddress
        every { anyConstructed<EidService>().getTcToken(any()) } returns mockTCToken
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
        .exchange()

    private fun retrieveIdentificationSession(useIDSessionId: UUID): IdentificationSession {
        return identificationSessionService.findByUseIDSessionId(useIDSessionId).block()
            ?: throw NoSuchElementException()
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
