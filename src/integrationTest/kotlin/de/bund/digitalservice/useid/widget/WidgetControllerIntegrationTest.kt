package de.bund.digitalservice.useid.widget

import de.bund.digitalservice.useid.config.CSP_DEFAULT_CONFIG
import de.bund.digitalservice.useid.config.CSP_FRAME_ANCESTORS_NONE
import de.bund.digitalservice.useid.config.CSP_FRAME_ANCESTORS_SELF
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.MessageSource
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import java.util.Locale
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WidgetControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val messageSource: MessageSource,
) {

    @AfterAll
    fun afterAll() {
        unmockkStatic(UUID::class)
    }

    val validTenantId = "integration_test_1"
    val allowedHost = "i.am.allowed.1"
    val forbiddenHost = "i.am.forbidden"
    val tcTokenUrl = "https://www.foo.bar"

    @Test
    fun `widget endpoint WIDGET_START_IDENT_BTN_CLICKED should return 200 when query parameter tenant_id has correct value`() {
        webTestClient
            .post()
            .uri("/$WIDGET_START_IDENT_BTN_CLICKED?tenant_id=$validTenantId")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `widget endpoint WIDGET_START_IDENT_BTN_CLICKED should return 200 when query parameter tenant_id is missing`() {
        webTestClient
            .post()
            .uri("/$WIDGET_START_IDENT_BTN_CLICKED")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `widget endpoint should disable X-Frame-Options`() {
        webTestClient
            .get()
            .uri("/widget?hostname=$allowedHost")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .doesNotExist("X-Frame-Options")
    }

    @Test
    fun `widget endpoint returns Content-Security-Policy with allowed host and nonce when the request contains a valid hostname parameter`() {
        // GIVEN
        val nonce = UUID.randomUUID().toString()
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns nonce

        webTestClient
            // WHEN
            .get()
            .uri("/widget?hostname=$allowedHost")
            .exchange()
            // THEN
            .expectStatus().isOk
            .expectHeader()
            .valueEquals(
                "Content-Security-Policy",
                "$CSP_DEFAULT_CONFIG;script-src 'self' 'nonce-$nonce';$CSP_FRAME_ANCESTORS_SELF $allowedHost;",
            )
            .expectHeader()
            .valueEquals(
                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                allowedHost,
            )
            .expectHeader()
            .valueEquals(HttpHeaders.VARY, HttpHeaders.ORIGIN)
    }

    @Test
    fun `widget page includes script to set href on eidClientButton`() {
        // GIVEN
        val nonce = UUID.randomUUID().toString()
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns nonce

        val result = webTestClient
            // WHEN
            .get()
            .uri("/widget?hostname=$allowedHost")
            .exchange()
            // THEN
            .expectStatus().isOk
            .expectBody()
            .returnResult()

        assertThat(result.responseBody!!.decodeToString()).contains("eidClientButton.setAttribute(\"href\", eidClientURL);")
    }

    @Test
    fun `widget endpoint returns 400 when query parameter hostname is not set`() {
        webTestClient
            .get()
            .uri("/widget")
            .exchange()
            .expectStatus()
            .isBadRequest
    }

    @Test
    fun `widget endpoint returns 401 when query parameter hostname contains forbidden value`() {
        webTestClient
            .get()
            .uri("/widget?hostname=$forbiddenHost")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `widget endpoint renders page correctly when the devices are supported`() {
        val compatibleAndroidUserAgent = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.5304.105 Mobile Safari/537.36"
        val compatibleIOSUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_1_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.1 Mobile/15E148 Safari/604.1"

        val (iosResponse, androidResponse) = fetchWidgetPageWithMobileDevices(compatibleAndroidUserAgent, compatibleIOSUserAgent)

        iosResponse.expectStatus().isOk
        androidResponse.expectStatus().isOk
    }

    @Test
    fun `widget endpoint renders widget page correctly when the user agents do not have proper OS version`() {
        val malformedAndroidUserAgent = "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.5304.105 Mobile Safari/537.36"
        val malformedIOSUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 Safari/604.1"

        val (iosResponse, androidResponse) = fetchWidgetPageWithMobileDevices(malformedAndroidUserAgent, malformedIOSUserAgent)

        iosResponse.expectStatus().isOk
        androidResponse.expectStatus().isOk
    }

    @Test
    fun `widget endpoint renders widget page correctly when the user agents are malformed`() {
        val malformedAndroidUserAgent = "Android Foo Bar"
        val malformedIOSUserAgent = "iPhone Foo Bar"

        val (iosResponse, androidResponse) = fetchWidgetPageWithMobileDevices(malformedAndroidUserAgent, malformedIOSUserAgent)

        iosResponse.expectStatus().isOk
        androidResponse.expectStatus().isOk
    }

    @Test
    fun `widget endpoint renders INCOMPATIBLE_PAGE when the devices are unsupported`() {
        val incompatibleAndroidUserAgent = "Mozilla/5.0 (Linux; Android 8.0.0; SM-G960F Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36"
        val incompatibleIOSUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1"

        val (iosResponse, androidResponse) = fetchWidgetPageWithMobileDevices(incompatibleAndroidUserAgent, incompatibleIOSUserAgent)

        val iOSResponseBody = iosResponse.expectBody().returnResult().responseBody?.decodeToString()
        val androidResponseBody = androidResponse.expectBody().returnResult().responseBody?.decodeToString()

        val errorTitle = messageSource.getMessage("widget.incompatible.headline-title", null, Locale.GERMAN)
        assertThat(iOSResponseBody).contains(errorTitle)
        assertThat(androidResponseBody).contains(errorTitle)
    }

    @Test
    fun `fallback page returns 200 with fallback class on container`() {
        // when
        val result = getFallbackPageResponseBody()
        val parsedResponseBody = Jsoup.parse(result)

        // then
        val containerFallback = parsedResponseBody.getElementsByClass("container").attr("class")
        assertThat(containerFallback).contains("fallback")
    }

    @Test
    fun `fallback page returns 200 and contains errorTitle`() {
        // when
        val result = getFallbackPageResponseBody()
        val parsedResponseBody = Jsoup.parse(result)

        // then
        val actualErrorTitle = parsedResponseBody.getElementsByClass("error_title").text()
        val expectedErrorTitle = messageSource.getMessage("error.default.title", null, Locale.GERMAN)
        assertThat(actualErrorTitle).contains(expectedErrorTitle)
    }

    @Test
    fun `fallback page returns Content-Security-Policy with nonce and disallowing use of frames`() {
        // given
        val nonce = UUID.randomUUID().toString()
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns nonce

        // when
        getFallbackPage()
            // then
            .expectStatus().isOk
            .expectHeader()
            .valueEquals(
                "Content-Security-Policy",
                "$CSP_DEFAULT_CONFIG;script-src 'self' 'nonce-$nonce';$CSP_FRAME_ANCESTORS_NONE;",
            )
    }

    @Test
    fun `fallback page shows identification button which links to eID-Client URL with bundesident scheme`() {
        // when
        val responseBody: String = getFallbackPageResponseBody()

        // then
        val eidClientButton = Jsoup.parse(responseBody).getElementById("eid-client-button")?.attr("href")
        assertThat(eidClientButton).startsWith("bundesident://127.0.0.1:24727/eID-Client")
    }

    @Test
    fun `fallback page encodes tcTokenURL param for identification button when the query param is passed`() {
        // when
        val result = getFallbackPageResponseBody()

        // then
        val eidClientButton = Jsoup.parse(result).getElementById("eid-client-button")?.attr("href")
        assertThat(eidClientButton).endsWith("?tcTokenURL=https%3A%2F%2Fwww.foo.bar")
    }

    @Test
    fun `fallback page does not include script to set eidClientButton href on fallback page`() {
        // when
        val result = getFallbackPageResponseBody()

        // then
        assertThat(result).doesNotContain("eidClientButton.setAttribute(\"href\", eidClientURL);")
    }

    private fun getFallbackPageResponseBody(): String {
        val result = getFallbackPage()
            .expectStatus().isOk
            .expectBody()
            .returnResult()

        return result.responseBody!!.decodeToString()
    }

    private fun getFallbackPage() = webTestClient
        .get()
        .uri("/$FALLBACK_PAGE?tcTokenURL=$tcTokenUrl")
        .exchange()

    private fun fetchWidgetPageWithMobileDevices(
        androidUserAgent: String,
        iosUserAgent: String,
    ): Pair<ResponseSpec, ResponseSpec> {
        val iOSResponse: ResponseSpec = webTestClient
            .get()
            .uri("/widget?hostname=$allowedHost")
            .header("User-Agent", androidUserAgent)
            .exchange()

        val androidResponse: ResponseSpec = webTestClient
            .get()
            .uri("/widget?hostname=$allowedHost")
            .header("User-Agent", iosUserAgent)
            .exchange()

        return Pair(iOSResponse, androidResponse)
    }
}
