package de.bund.digitalservice.useid.widget

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

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
    fun `widget endpoint returns default Content-Security-Policy when query parameter is not set`() {
        webTestClient
            .get()
            .uri("/widget")
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
    fun `widget endpoint renders page correctly when the devices are supported`() {
        val compatibleAndroidUserAgent = "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.5304.105 Mobile Safari/537.36"
        val compatibleIOSUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_1_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.1 Mobile/15E148 Safari/604.1"

        webTestClient
            .get()
            .uri("/widget")
            .header("User-Agent", compatibleAndroidUserAgent)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .get()
            .uri("/widget")
            .header("User-Agent", compatibleIOSUserAgent)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `widget endpoint renders widget page correctly when the user agents do not have proper OS version`() {
        val malformedAndroidUserAgent = "Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.5304.105 Mobile Safari/537.36"
        val malformedIOSUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 Safari/604.1"

        webTestClient
            .get()
            .uri("/widget")
            .header("User-Agent", malformedAndroidUserAgent)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .get()
            .uri("/widget")
            .header("User-Agent", malformedIOSUserAgent)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `widget endpoint renders widget page correctly when the user agents are malformed`() {
        val malformedAndroidUserAgent = "Android Foo Bar"
        val malformedIOSUserAgent = "iPhone Foo Bar"

        webTestClient
            .get()
            .uri("/widget")
            .header("User-Agent", malformedAndroidUserAgent)
            .exchange()
            .expectStatus()
            .isOk

        webTestClient
            .get()
            .uri("/widget")
            .header("User-Agent", malformedIOSUserAgent)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `widget endpoint redirects to INCOMPATIBLE_PAGE when the devices are unsupported`() {
        val incompatibleAndroidUserAgent = "Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"
        val incompatibleIOSUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"

        webTestClient
            .get()
            .uri("/widget")
            .header("User-Agent", incompatibleAndroidUserAgent)
            .exchange()
            .expectHeader()
            .location("/incompatible")
            .expectStatus()
            .is3xxRedirection

        webTestClient
            .get()
            .uri("/widget")
            .header("User-Agent", incompatibleIOSUserAgent)
            .exchange()
            .expectHeader()
            .location("/incompatible")
            .expectStatus()
            .is3xxRedirection
    }

    @Test
    fun `widget endpoint INCOMPATIBLE_PAGE should return 200 and should contain headlineTitle`() {
        val result = webTestClient
            .get()
            .uri("/$INCOMPATIBLE_PAGE")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()

        val body = String(result.responseBody!!)
        assertThat(body, containsString(widgetProperties.errorView.incompatible.localization.headlineTitle))
    }

    @Test
    fun `widget endpoint FALLBACK_PAGE should return 200 and should contain errorTitle`() {
        val result = webTestClient
            .get()
            .uri("/$FALLBACK_PAGE")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()

        val body = String(result.responseBody!!)
        assertThat(body, containsString(widgetProperties.errorView.fallback.localization.errorTitle))
        assertThat(body, containsString("class=\"container fallback\""))
    }

    @Test
    fun `widget endpoint APP_OPENED should return 200`() {
        webTestClient
            .post()
            .uri("/$WIDGET_START_IDENT_BTN_CLICKED")
            .exchange()
            .expectStatus().isOk
    }
}
