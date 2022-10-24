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
    fun `widget endpoint INCOMPATIBLE_PAGE should return 200`() {
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
    fun `widget endpoint FALLBACK_PAGE should return 200`() {
        val result = webTestClient
            .get()
            .uri("/$FALLBACK_PAGE")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()

        val body = String(result.responseBody!!)
        assertThat(body, containsString(widgetProperties.errorView.fallback.localization.errorTitle))
    }
}
