package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.eidservice.EidService
import io.mockk.every
import io.mockk.mockkConstructor
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class TcTokenControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) {

    @Autowired
    private lateinit var identificationSessionRepository: IdentificationSessionRepository

    @BeforeAll
    fun setup() {
        mockkConstructor(EidService::class)

        Awaitility.setDefaultTimeout(Durations.ONE_SECOND)
    }

    @Test
    fun `tcToken endpoint returns valid tc-token and sets correct eIdSessionId in IdentificationSession`() {
        var tcTokenURL = ""
        webTestClient.sendStartSessionRequest()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        val eIdSessionId = UUID.randomUUID()

        mockTcToken("https://www.foobar.com?sessionId=$eIdSessionId")

        webTestClient.createGETRequest(extractRelativePathFromURL(tcTokenURL))
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_XML)
            .expectBody().xpath("TCTokenType").exists()

        val useIdSessionId = extractUseIdSessionIdFromTcTokenUrl(tcTokenURL)
        val session = identificationSessionRepository.retrieveIdentificationSession(useIdSessionId)!!
        Assertions.assertEquals(eIdSessionId, session.eIdSessionId)
    }

    @Test
    fun `tcToken endpoint returns 400 when passed an invalid UUID as useIdSessionID`() {
        val invalidId = "IamInvalid"
        webTestClient.createGETRequest("/api/v1/tc-tokens/$invalidId").exchange().expectStatus().isBadRequest
    }

    @Test
    fun `tcToken endpoint returns 404 when passed a unknown UUID as useIdSessionID`() {
        val unknownId = UUID.randomUUID()
        webTestClient.createGETRequest("/api/v1/tc-tokens/$unknownId").exchange().expectStatus().isNotFound
    }

    @Test
    fun `tcToken endpoint returns 500 when error is thrown`() {
        every { anyConstructed<EidService>().getTcToken(any()) } throws Error("internal server error")

        var tcTokenURL = ""
        webTestClient.sendStartSessionRequest()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        webTestClient.createGETRequest(extractRelativePathFromURL(tcTokenURL)).exchange().expectStatus().is5xxServerError
    }
}
