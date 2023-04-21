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
import org.awaitility.Awaitility
import org.awaitility.Awaitility.await
import org.awaitility.Durations.ONE_SECOND
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
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

private const val AUTHORIZATION_HEADER = "Bearer valid-api-key-1"
private const val REFRESH_ADDRESS = "valid-refresh-address-1"
const val TEST_IDENTIFICATION_SESSIONS_BASE_PATH = "api/v1/identifications"
const val TEST_IDENTIFICATION_SESSIONS_OLD_BASE_PATH = "api/v1/identification/sessions"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class IdentificationSessionControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) {
    fun getIdentificationPath(forOldApi: Boolean = false): String {
        return if (forOldApi) {
            "${applicationProperties.baseUrl}/$TEST_IDENTIFICATION_SESSIONS_OLD_BASE_PATH"
        } else {
            "${applicationProperties.baseUrl}/$TEST_IDENTIFICATION_SESSIONS_BASE_PATH"
        }
    }

    val attributes = listOf("DG1", "DG2")

    @Autowired
    private lateinit var identificationSessionRepository: IdentificationSessionRepository

    @Autowired
    private lateinit var applicationProperties: ApplicationProperties

    @BeforeAll
    fun setup() {
        mockkConstructor(EidService::class)

        Awaitility.setDefaultTimeout(ONE_SECOND)
    }

    @Test
    fun `start session endpoint returns TCTokenUrl`() {
        var tcTokenURL = ""

        sendStartSessionRequest()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        val useIdSessionId = extractUseIdSessionIdFromTcTokenUrl(tcTokenURL)
        val session = retrieveIdentificationSession(useIdSessionId)!!
        assertThat(session.eIdSessionId, nullValue())
        assertThat(session.useIdSessionId, notNullValue())
        assertThat(session.requestDataGroups, `is`(attributes))
        assertThat(session.refreshAddress, `is`(REFRESH_ADDRESS))

        val expectedTcTokenURL = "${getIdentificationPath(forOldApi = true)}/${session.useIdSessionId}/tc-token"
        assertEquals(expectedTcTokenURL, tcTokenURL)
    }

    @Test
    fun `start session endpoint returns 403 when no authorization header was passed`() {
        sendGETRequest(IDENTIFICATIONS_BASE_PATH).exchange().expectStatus().isForbidden
    }

    @Test
    fun `tcToken endpoint returns valid tc-token and sets correct eIdSessionId in IdentificationSession`() {
        var tcTokenURL = ""
        sendStartSessionRequest()
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

        val useIdSessionId = extractUseIdSessionIdFromTcTokenUrl(tcTokenURL)
        val session = retrieveIdentificationSession(useIdSessionId)!!
        assertEquals(eIdSessionId, session.eIdSessionId)
    }

    @Test
    fun `tcToken endpoint returns 400 when passed an invalid UUID as useIdSessionID`() {
        val invalidId = "IamInvalid"
        sendGETRequest("$TEST_IDENTIFICATION_SESSIONS_OLD_BASE_PATH/$invalidId/tc-token").exchange().expectStatus().isBadRequest
    }

    @Test
    fun `tcToken endpoint returns 404 when passed a unknown UUID as useIdSessionID`() {
        val unknownId = UUID.randomUUID()
        sendGETRequest("$TEST_IDENTIFICATION_SESSIONS_OLD_BASE_PATH/$unknownId/tc-token").exchange().expectStatus().isNotFound
    }

    @Test
    fun `tcToken endpoint returns 500 when error is thrown`() {
        every { anyConstructed<EidService>().getTcToken(any()) } throws Error("internal server error")

        var tcTokenURL = ""
        sendStartSessionRequest()
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
        sendStartSessionRequest()
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

        val useIdSessionId = extractUseIdSessionIdFromTcTokenUrl(tcTokenURL)
        await().until { retrieveIdentificationSession(useIdSessionId) == null }
    }

    @Test
    fun `identity data endpoint returns 400 when passed an invalid string instead of UUID`() {
        sendIdentityRequest("IamInvalid")
            .expectStatus().isBadRequest
    }

    @Test
    fun `identity data endpoint returns 401 when api key differs from the api key used to create the session`() {
        val eIdSessionId = UUID.randomUUID().toString()
        mockTcToken("https://www.foobar.com?sessionId=$eIdSessionId")

        var tcTokenURL = ""
        webTestClient
            .post()
            .uri(TEST_IDENTIFICATION_SESSIONS_BASE_PATH)
            .headers {
                it.set(HttpHeaders.AUTHORIZATION, "Bearer valid-api-key-2")
            }
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        sendGETRequest(extractRelativePathFromURL(tcTokenURL))
            .exchange()
            .expectStatus().isOk

        sendIdentityRequest(eIdSessionId)
            .expectStatus().isUnauthorized
    }

    @Test
    fun `identity data endpoint returns 403 when no authorization header was passed`() {
        sendGETRequest("$TEST_IDENTIFICATION_SESSIONS_BASE_PATH/${UUID.randomUUID()}").exchange().expectStatus().isForbidden
    }

    @Test
    fun `identity data endpoint returns 404 when passed a random UUID`() {
        sendIdentityRequest(UUID.randomUUID().toString())
            .expectStatus().isNotFound
    }

    private fun mockTcToken(refreshAddress: String) {
        val mockTCToken = mockk<TCTokenType>(relaxed = true)
        every { mockTCToken.refreshAddress } returns refreshAddress
        every { anyConstructed<EidService>().getTcToken(any()) } returns mockTCToken
    }

    private fun sendIdentityRequest(eIdSessionId: String) =
        sendGETRequest("$TEST_IDENTIFICATION_SESSIONS_BASE_PATH/$eIdSessionId")
            .headers { setAuthorizationHeader(it) }
            .exchange()

    private fun extractRelativePathFromURL(url: String) = URI.create(url).rawPath

    private fun sendGETRequest(uri: String) = webTestClient
        .get()
        .uri(uri)

    private fun sendStartSessionRequest() = webTestClient
        .post()
        .uri(TEST_IDENTIFICATION_SESSIONS_BASE_PATH)
        .headers { setAuthorizationHeader(it) }
        .exchange()

    private fun retrieveIdentificationSession(useIdSessionId: UUID): IdentificationSession? {
        return identificationSessionRepository.findByUseIdSessionId(useIdSessionId)
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
