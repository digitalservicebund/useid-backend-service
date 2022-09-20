package de.bund.digitalservice.useid.refresh

import de.bund.digitalservice.useid.eidservice.EidService
import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import java.net.URI
import java.util.UUID

private const val AUTHORIZATION_HEADER = "Bearer some-api-key"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("test")
class RefreshControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) {

    @BeforeAll
    fun setup() {
        mockkConstructor(EidService::class)
    }

    @Test
    fun `refresh endpoint redirect client to correct refresh address when eidSessionId is valid`() {
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
            .uri("/refresh?sessionId=$eIdSessionId")
            .exchange()
            .expectStatus()
            .is3xxRedirection
    }

    @Test
    fun `refresh endpoint responds with status code 404 when eidSessionId is invalid`() {
        webTestClient
            .get()
            .uri("/refresh?sessionId=${UUID.randomUUID()}")
            .exchange()
            .expectStatus()
            .isNotFound
    }
}
