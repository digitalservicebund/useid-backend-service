package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.config.TestApplicationProperties
import de.bund.digitalservice.useid.config.TestConfig
import mu.KotlinLogging
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.net.URI

@ExtendWith(SpringExtension::class)
@Tag("journey")
@Import(TestConfig::class)
class IdentificationSessionJourneyTest {
    private val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var testApplicationProperties: TestApplicationProperties

    @Test
    fun `create identification session and fetch tc token`() {
        val webTestClient = WebTestClient.bindToServer()
            .baseUrl(testApplicationProperties.staging!!.url)
            .build()

        var tcTokenUrl = ""
        webTestClient
            .post()
            .uri("/api/v1/identification/sessions")
            .headers { setAuthorizationHeader(it) }
            // TODO: REMOVE ATTRIBUTES WHEN TICKET USEID-299 IS FINISHED
            .body(BodyInserters.fromValue(CreateIdentitySessionRequest(listOf("DG1", "DG2"))))
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.tcTokenUrl").value<String> { tcTokenUrl = it }

        log.info("TC Token URL: $tcTokenUrl")

        val returnResult = webTestClient
            .get()
            .uri(URI.create(tcTokenUrl))
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
            .returnResult()

        validateSessionIdParameter(returnResult)
    }

    private fun validateSessionIdParameter(returnResult: EntityExchangeResult<ByteArray>) {
        val tcToken = String(returnResult.responseBody!!, Charsets.UTF_8)
        val uuidPattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
        assertThat(tcToken, matchesPattern("^(.*?)sessionId=$uuidPattern.*"))
    }

    private fun setAuthorizationHeader(headers: HttpHeaders) {
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer ${testApplicationProperties.staging!!.apiKey}")
    }
}
