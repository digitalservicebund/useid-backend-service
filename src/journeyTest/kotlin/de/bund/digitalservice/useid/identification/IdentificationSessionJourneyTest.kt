package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.config.JourneyTestApplicationProperties
import de.bund.digitalservice.useid.config.JourneyTestConfig
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import java.net.URI
import java.time.Duration

const val TEST_IDENTIFICATIONS_BASE_PATH = "api/v1/identifications"

@ExtendWith(SpringExtension::class)
@Import(JourneyTestConfig::class)
class IdentificationSessionJourneyTest(@Autowired private val journeyTestApplicationProperties: JourneyTestApplicationProperties) {
    private val log = KotlinLogging.logger {}

    @Test
    fun `create identification session and fetch tc token`() {
        val webTestClient = WebTestClient.bindToServer()
            .baseUrl(journeyTestApplicationProperties.staging!!.url)
            .responseTimeout(Duration.ofSeconds(10))
            .build()

        var tcTokenURL = ""
        webTestClient
            .post()
            .uri(TEST_IDENTIFICATIONS_BASE_PATH)
            .headers { setAuthorizationHeader(it) }
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        log.info("TC Token URL: $tcTokenURL")

        val returnResult = webTestClient
            .get()
            .uri(extractRelativePathFromURL(tcTokenURL))
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
        assertThat(tcToken).matches("^(.*?)sessionId=$uuidPattern.*")
    }

    private fun setAuthorizationHeader(headers: HttpHeaders) {
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer ${journeyTestApplicationProperties.staging!!.apiKey}")
    }

    private fun extractRelativePathFromURL(url: String) = URI.create(url).rawPath
}
