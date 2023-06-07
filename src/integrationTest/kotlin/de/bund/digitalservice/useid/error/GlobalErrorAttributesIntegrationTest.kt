package de.bund.digitalservice.useid.error

import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriUtils
import java.time.Duration
import java.util.stream.Stream

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GlobalErrorAttributesIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
) {
    private val subject404 = "Fehlermeldung mit Status 404"
    private val body404 =
        """
           Liebes BundesIdent-Team, 

           Ich habe erneut eine Fehlermeldung mit dem Status 404 erhalten. 

           Ich wollte mich bei folgendem Diensteanbieter identifizieren: 
           Der Fehler trat auf, nachdem ich: 
           Ich benutze folgendes Gerät: 
           Das Betriebssystem meines Gerätes: 
           Ich benutze folgenden Browser: 
           Die Version von meinem Browser: 

           Mit freundlichen Grüßen
        """.trimIndent()

    @ParameterizedTest
    @MethodSource("acceptedMediaTypesContainingHtml")
    fun `error attributes added if accepted media type contains html`(acceptHeader: String) {
        val webTestClientWithHigherTimeout = webTestClient.mutate().responseTimeout(Duration.ofSeconds(10)).build()
        // given
        val response = webTestClientWithHigherTimeout.get()
            .uri("/foo/bar")
            .header(HttpHeaders.ACCEPT, acceptHeader)
            // when
            .exchange()
            .expectStatus().is4xxClientError
            .expectHeader().contentType("${MediaType.TEXT_HTML_VALUE};charset=UTF-8")

        val responseBody = response.expectBody().returnResult().responseBody?.decodeToString()
        val parsedResponseBody = Jsoup.parse(responseBody!!)

        // then
        assertThat(parsedResponseBody.select("a").attr("href")).isEqualTo(expectedReportEmailLink(subject404, body404))
    }

    private fun acceptedMediaTypesContainingHtml(): Stream<Arguments?>? {
        return Stream.of(
            Arguments.of(MediaType.TEXT_HTML_VALUE),
            Arguments.of("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"),
            Arguments.of("application/xhtml+xml,application/xml,text/html"),
        )
    }

    @ParameterizedTest
    @MethodSource("acceptedMediaTypesContainsJson")
    fun `error attributes not added if accepted media type contains json`(acceptHeader: String) {
        // given
        val nonExistingPath = "/foo/bar"

        // when
        webTestClient.get()
            .uri(nonExistingPath)
            .header(HttpHeaders.ACCEPT, acceptHeader)
            .exchange()
            // then
            .expectStatus().is4xxClientError
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.path").isEqualTo(nonExistingPath)
            .jsonPath("$.$SHOW_REPORT_EMAIL").doesNotExist()
            .jsonPath("$.$ERROR_REPORT_EMAIL_LINK").doesNotExist()
    }

    private fun acceptedMediaTypesContainsJson(): Stream<Arguments?>? {
        return Stream.of(
            Arguments.of(MediaType.APPLICATION_JSON_VALUE),
            Arguments.of("application/json,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"),
        )
    }

    fun expectedReportEmailLink(subject: String, body: String): String {
        val encodedSubject = UriUtils.encode(subject, Charsets.UTF_8)
        val encodedBody = UriUtils.encode(body, Charsets.UTF_8)

        return "mailto:hilfe@bundesident.de?subject=$encodedSubject&body=$encodedBody"
    }
}
