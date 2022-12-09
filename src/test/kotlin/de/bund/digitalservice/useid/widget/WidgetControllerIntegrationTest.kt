package de.bund.digitalservice.useid.widget

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WidgetControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) : PostgresTestcontainerIntegrationTest() {

    @Autowired
    private lateinit var widgetProperties: WidgetProperties

    @Test
    fun `widget endpoint should disable X-Frame-Options`() {
        webTestClient
            .get()
            .uri("/widget?hostname=foo.bar")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .doesNotExist("X-Frame-Options")
    }

    @Test
    fun `widget endpoint returns Content-Security-Policy with allowed host when the request URL is valid`() {
        webTestClient
            .get()
            .uri("/widget?hostname=foo.bar")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .valueEquals(
                "Content-Security-Policy",
                "some default value;frame-ancestors 'self' foo.bar;"
            )
    }

    @Test
    fun `widget endpoint returns default Content-Security-Policy when the request URL is invalid`() {
        webTestClient
            .get()
            .uri("/widget?hostname=not-allowed.com")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .valueEquals(
                "Content-Security-Policy",
                "some default value;frame-ancestors 'self';"
            )
    }

    @Test
    fun `widget endpoint returns default Content-Security-Policy when query parameter is not set with error`() {
        webTestClient
            .get()
            .uri("/widget")
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectHeader()
            .valueEquals(
                "Content-Security-Policy",
                "some default value;frame-ancestors 'self';"
            )
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

        assertThat(iOSResponseBody, containsString(widgetProperties.errorView.incompatible.localization.headlineTitle))
        assertThat(androidResponseBody, containsString(widgetProperties.errorView.incompatible.localization.headlineTitle))
    }

    @Test
    fun `widget endpoint FALLBACK_PAGE should return 200 and should contain errorTitle`() {
        val result = webTestClient
            .get()
            .uri("/$FALLBACK_PAGE?tcTokenURL=foobar")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()

        val responseBody: String? = result.responseBody?.decodeToString()
        val parsedResponseBody = Jsoup.parse(responseBody!!)

        val containerFallback = parsedResponseBody.getElementsByClass("container").attr("class")
        val hasValidFallbackClassName = containsString("fallback")

        assertThat(containerFallback, hasValidFallbackClassName)

        val errorTitle = parsedResponseBody.getElementsByClass("error_title").text()
        val hasValidErrorTitle = containsString(widgetProperties.errorView.fallback.localization.errorTitle)

        assertThat(errorTitle, hasValidErrorTitle)
    }

    @Test
    fun `fallback page should encode tcTokenURL param for identification button when the query param is passed`() {
        val tcTokenUrl = "https://www.foo.bar"
        val result = webTestClient
            .get()
            .uri("/$FALLBACK_PAGE?tcTokenURL=$tcTokenUrl") // tcTokenURL is automatically encoded by webTestClient
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()

        val responseBody: String? = result.responseBody?.decodeToString()
        val parsedResponseBody = Jsoup.parse(responseBody!!)

        val eidClientButton = parsedResponseBody.getElementById("eid-client-button")?.attr("href")
        val hasCorrectUrl = containsString("bundesident://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Fwww.foo.bar")

        assertThat(eidClientButton, hasCorrectUrl)
    }

    @Test
    fun `widget endpoint APP_OPENED should return 200`() {
        webTestClient
            .post()
            .uri("/$WIDGET_START_IDENT_BTN_CLICKED")
            .exchange()
            .expectStatus().isOk
    }

    private fun fetchWidgetPageWithMobileDevices(
        androidUserAgent: String,
        iosUserAgent: String
    ): Pair<ResponseSpec, ResponseSpec> {
        val iOSResponse: ResponseSpec = webTestClient
            .get()
            .uri("/widget?hostname=foo.bar")
            .header("User-Agent", androidUserAgent)
            .exchange()

        val androidResponse: ResponseSpec = webTestClient
            .get()
            .uri("/widget?hostname=foo.bar")
            .header("User-Agent", iosUserAgent)
            .exchange()

        return Pair(iOSResponse, androidResponse)
    }
}
