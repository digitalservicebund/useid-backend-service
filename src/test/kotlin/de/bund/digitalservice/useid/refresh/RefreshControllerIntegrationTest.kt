package de.bund.digitalservice.useid.refresh

import de.bund.digitalservice.useid.eidservice.EidService
import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import java.net.URI
import java.util.UUID

private const val AUTHORIZATION_HEADER = "Bearer some-api-key"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RefreshControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) : PostgresTestcontainerIntegrationTest() {

    @BeforeAll
    fun setup() {
        mockkConstructor(EidService::class)
    }

    @AfterAll
    fun afterTests() {
        unmockkAll()
    }

    @Test
    fun `refresh endpoint redirects client to correct refresh address when eIdSessionId is valid`() {
        var tcTokenURL = ""
        val eIdSessionId = UUID.randomUUID()
        val mockTCToken = mockk<TCTokenType>()

        every { anyConstructed<EidService>().getTcToken(any()) } returns mockTCToken
        every { mockTCToken.refreshAddress } returns "https://www.foobar.com?sessionId=$eIdSessionId"

        webTestClient
            .post()
            .uri("/api/v1/identification/sessions")
            .headers { it.set(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER) }
            .exchange()
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        webTestClient
            .get()
            .uri(URI.create(tcTokenURL).rawPath)
            .exchange()

        webTestClient
            .get()
            .uri("/refresh?sessionId=$eIdSessionId&error=false&test=123")
            .exchange()
            .expectStatus()
            .is3xxRedirection
            .expectHeader()
            // "some-refresh-address" value is defined in the test application.yaml
            // Validate if all request parameters are forwarded
            .location("some-refresh-address?sessionId=$eIdSessionId&error=false&test=123")
    }

    @Test
    fun `refresh endpoint responds with status code 404 when eIdSessionId is invalid`() {
        webTestClient
            .get()
            .uri("/refresh?sessionId=${UUID.randomUUID()}")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
