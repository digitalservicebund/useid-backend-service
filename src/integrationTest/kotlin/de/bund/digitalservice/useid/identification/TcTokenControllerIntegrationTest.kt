package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.eidservice.EidService
import io.mockk.every
import io.mockk.mockkConstructor
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

        webTestClient.sendGETRequest(extractRelativePathFromURL(tcTokenURL))
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_XML)
            .expectBody().xpath("TCTokenType").exists()

        val useIdSessionId = extractUseIdSessionIdFromTcTokenUrl(tcTokenURL)
        val session = identificationSessionRepository.retrieveIdentificationSession(useIdSessionId)!!
        assertThat(session.eIdSessionId).isEqualTo(eIdSessionId)
    }

    @Test
    fun `tcToken endpoint returns 400 when passed an invalid UUID as useIdSessionID`() {
        val invalidId = "IamInvalid"
        webTestClient.sendGETRequest("/api/v1/tc-tokens/$invalidId").expectStatus().isBadRequest
    }

    @Test
    fun `tcToken endpoint returns 404 when passed a unknown UUID as useIdSessionID`() {
        val unknownId = UUID.randomUUID()
        webTestClient.sendGETRequest("/api/v1/tc-tokens/$unknownId").expectStatus().isNotFound
    }

    @Test
    fun `tcToken endpoint returns 406 when accepted media type is not XML`() {
        var tcTokenURL = ""
        webTestClient.sendStartSessionRequest()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        webTestClient.sendGETRequest(extractRelativePathFromURL(tcTokenURL))
            .accept(MediaType.TEXT_HTML)
            .exchange()
            .expectStatus().isEqualTo(406)
    }

    @Test
    fun `tcToken endpoint returns 500 when error is thrown`() {
        every { anyConstructed<EidService>().getTcToken(any()) } throws Error("internal server error")

        var tcTokenURL = ""
        webTestClient.sendStartSessionRequest()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        webTestClient.sendGETRequest(extractRelativePathFromURL(tcTokenURL)).expectStatus().is5xxServerError
    }
}
