package de.bund.digitalservice.useid.statics

import de.bund.digitalservice.useid.eidservice.EidService
import io.mockk.every
import io.mockk.mockkConstructor
import org.apache.http.client.utils.URIBuilder
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class GlobalStaticErrorIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
) {
    @Test
    fun `global error handler should render error page when a client requests to invalid path`() {
        val client = webTestClient.get()
            .uri("/widget/useid-backend-team-is-here")
            .accept(MediaType.TEXT_HTML) // WebTestClient will return JSON response by default, and we need an HTML page
            .exchange()

        client.expectStatus().is4xxClientError

        val responseBody = client.expectBody().returnResult().responseBody?.decodeToString()
        val parsedResponseBody = Jsoup.parse(responseBody!!)

        assertTrue(parsedResponseBody.select("img").hasClass("error_icon"))
        assertThat(parsedResponseBody.select("a").attr("href"), containsString("mailto:hilfe@bundesident.de"))
    }

    @Test
    fun `global error handler should render error page when a client makes invalid request to identification endpoint`() {
        mockkConstructor(EidService::class)

        every { anyConstructed<EidService>().getTcToken(any()) } throws Error("internal server error")

        var tcTokenURL = ""
        webTestClient.post()
            .uri("/api/v1/identification/sessions")
            .header(HttpHeaders.AUTHORIZATION, "Bearer valid-api-key-1")
            .exchange()
            .expectBody()
            .jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        val tcTokenUrlPathParam = URIBuilder(tcTokenURL).path

        val client = webTestClient.get()
            .uri(tcTokenUrlPathParam)
            .accept(MediaType.TEXT_HTML)
            .exchange()

        // Since the tc-token endpoint accepts JSON, the backend will return 406 NOT ACCEPTABLE
        // instead of 500 Internal Server Error
        client.expectStatus().is4xxClientError

        val responseBody = client.expectBody().returnResult().responseBody?.decodeToString()
        val parsedResponseBody = Jsoup.parse(responseBody!!)

        assertTrue(parsedResponseBody.select("img").hasClass("error_icon"))
        assertThat(parsedResponseBody.select("a").attr("href"), containsString("mailto:hilfe@bundesident.de"))
    }

    @Test
    fun `global error handler should render error page when a client makes invalid request to refresh endpoint`() {
        val client = webTestClient
            .get()
            .uri("/refresh")
            .accept(MediaType.TEXT_HTML)
            .exchange()

        client.expectStatus().is4xxClientError

        val responseBody = client.expectBody().returnResult().responseBody?.decodeToString()
        val parsedResponseBody = Jsoup.parse(responseBody!!)

        assertTrue(parsedResponseBody.select("img").hasClass("error_icon"))
        assertThat(parsedResponseBody.select("a").attr("href"), containsString("mailto:hilfe@bundesident.de"))
    }
}
