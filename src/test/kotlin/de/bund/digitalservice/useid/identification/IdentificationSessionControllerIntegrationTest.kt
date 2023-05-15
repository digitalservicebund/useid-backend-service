package de.bund.digitalservice.useid.identification

import de.bund.bsi.eid230.GetResultResponseType
import de.bund.bsi.eid230.LevelOfAssuranceType
import de.bund.bsi.eid230.OperationsResponderType
import de.bund.bsi.eid230.PersonalDataType
import de.bund.bsi.eid230.TransactionAttestationResponseType
import de.bund.bsi.eid230.VerificationResultType
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.eidservice.EidService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import oasis.names.tc.dss._1_0.core.schema.Result
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.awaitility.Awaitility.await
import org.awaitility.Durations.ONE_SECOND
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

private const val REFRESH_ADDRESS = "valid-refresh-address-1"
const val TEST_IDENTIFICATIONS_BASE_PATH = "api/v1/identifications"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class IdentificationSessionControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) {

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
    fun `start session endpoint creates correct identification session`() {
        var tcTokenURL = ""

        webTestClient.sendStartSessionRequest()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        val useIdSessionId = extractUseIdSessionIdFromTcTokenUrl(tcTokenURL)
        val session = identificationSessionRepository.retrieveIdentificationSession(useIdSessionId)!!
        assertThat(session.eIdSessionId).isNull()
        assertThat(session.useIdSessionId).isNotNull
        assertThat(session.requestDataGroups).isEqualTo(attributes)
        assertThat(session.refreshAddress).isEqualTo(REFRESH_ADDRESS)
    }

    @Test
    fun `start session endpoint returns correct TCTokenUrl`() {
        var tcTokenURL = ""

        webTestClient.sendStartSessionRequest()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        val useIdSessionId = extractUseIdSessionIdFromTcTokenUrl(tcTokenURL)

        val expectedTcTokenURL = "${applicationProperties.baseUrl}/api/v1/tc-tokens/$useIdSessionId"
        assertThat(tcTokenURL).isEqualTo(expectedTcTokenURL)
    }

    @Test
    fun `start session endpoint returns 403 when no authorization header was passed`() {
        webTestClient.sendGETRequest(TEST_IDENTIFICATIONS_BASE_PATH).exchange().expectStatus().isForbidden
    }

    @Test
    fun `identity data endpoint returns valid personal data and removes identification session from database`() {
        val eIdSessionId = UUID.randomUUID().toString()
        mockTcToken("https://www.foobar.com?sessionId=$eIdSessionId")

        var tcTokenURL = ""
        webTestClient.sendStartSessionRequest()
            .expectStatus().isOk
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        webTestClient.sendGETRequest(extractRelativePathFromURL(tcTokenURL))
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

        webTestClient.sendIdentityRequest(eIdSessionId)
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.result").value<LinkedHashMap<String, String>> {
                assertThat(it["resultMajor"]).isEqualTo(mockResult.resultMajor)
            }
            .jsonPath("$.personalData").value<LinkedHashMap<String, String>> {
                assertThat(it["givenNames"]).isEqualTo(personalData.givenNames)
            }

        val useIdSessionId = extractUseIdSessionIdFromTcTokenUrl(tcTokenURL)
        await().until { identificationSessionRepository.retrieveIdentificationSession(useIdSessionId) == null }
    }

    @Test
    fun `identity data endpoint returns 400 when passed an invalid string instead of UUID`() {
        webTestClient.sendIdentityRequest("IamInvalid")
            .expectStatus().isBadRequest
    }

    @Test
    fun `identity data endpoint returns 401 when api key differs from the api key used to create the session`() {
        val eIdSessionId = UUID.randomUUID().toString()
        mockTcToken("https://www.foobar.com?sessionId=$eIdSessionId")

        var tcTokenURL = ""
        webTestClient
            .post()
            .uri(TEST_IDENTIFICATIONS_BASE_PATH)
            .headers {
                it.set(HttpHeaders.AUTHORIZATION, "Bearer valid-api-key-2")
            }
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        webTestClient.sendGETRequest(extractRelativePathFromURL(tcTokenURL))
            .exchange()
            .expectStatus().isOk

        webTestClient.sendIdentityRequest(eIdSessionId)
            .expectStatus().isUnauthorized
    }

    @Test
    fun `identity data endpoint returns 403 when no authorization header was passed`() {
        webTestClient.sendGETRequest("$TEST_IDENTIFICATIONS_BASE_PATH/${UUID.randomUUID()}").exchange().expectStatus().isForbidden
    }

    @Test
    fun `identity data endpoint returns 404 when passed a random UUID`() {
        webTestClient.sendIdentityRequest(UUID.randomUUID().toString())
            .expectStatus().isNotFound
    }

    private fun WebTestClient.sendIdentityRequest(eIdSessionId: String) =
        this.sendGETRequest("$TEST_IDENTIFICATIONS_BASE_PATH/$eIdSessionId")
            .headers { setAuthorizationHeader(it) }
            .exchange()
}
