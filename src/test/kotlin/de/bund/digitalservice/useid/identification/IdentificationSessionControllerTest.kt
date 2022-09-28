package de.bund.digitalservice.useid.identification

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.useid.apikeys.ApiKeyAuthenticationToken
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.eidservice.EidService
import de.bund.digitalservice.useid.eidservice.EidServiceConfig
import de.bund.digitalservice.useid.eidservice.EidServiceProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.UUID

private const val AUTHORIZATION_HEADER = "Bearer some-api-key"

private const val REFRESH_ADDRESS = "some-refresh-address"

@Tag("test")
@ExtendWith(value = [OutputCaptureExtension::class, SpringExtension::class])
@WebFluxTest(controllers = [IdentificationSessionsController::class])
@Import(value = [ApplicationProperties::class, EidServiceConfig::class, EidServiceProperties::class])
@WithMockUser
class IdentificationSessionControllerTest(@Autowired val webTestClient: WebTestClient) {

    @MockkBean
    private lateinit var identificationSessionService: IdentificationSessionService

    @Test
    fun `get identity data endpoint returns 401 when refreshAddress of passed APIKey does not match the refreshAddress stored in the session`() {
        mockApiKeyAuthentication()
        mockFindSession("_ThisIsDifferent_")

        sendIdentityRequest()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `get identity data endpoint - eidService getEidInformation method should log error message`(output: CapturedOutput) {
        mockApiKeyAuthentication()
        mockFindSession(REFRESH_ADDRESS)

        mockkConstructor(EidService::class)
        every { anyConstructed<EidService>().getEidInformation(any()) } throws Error("log that!")

        sendIdentityRequest()
            .expectStatus()
            .is5xxServerError

        assertThat(output.all, containsString("log that!"))
    }

    @Test
    fun `get identity data endpoint should log resultMinor in case of error while fetching the data from the eid server`(output: CapturedOutput) {
        // TODO: ADD TEST FOR RESULT MINOR ERROR LOGGING
    }

    private fun mockFindSession(refreshAddress: String) {
        val mockSession = mockk<IdentificationSession>()
        every { mockSession.refreshAddress } returns refreshAddress
        every { identificationSessionService.findByEIDSessionId(any()) } returns Mono.just(mockSession)
    }

    private fun mockApiKeyAuthentication() {
        val apiKeyAuthenticationToken = ApiKeyAuthenticationToken("some-api-keys", REFRESH_ADDRESS)
        apiKeyAuthenticationToken.isAuthenticated = true
        SecurityContextHolder.getContext().authentication = apiKeyAuthenticationToken
    }

    private fun sendIdentityRequest(eIdSessionId: UUID = UUID.randomUUID()) =
        webTestClient
            .get()
            .uri("/api/v1/identification/sessions/$eIdSessionId")
            .headers { setAuthorizationHeader(it) }
            .exchange()

    private fun setAuthorizationHeader(headers: HttpHeaders) {
        headers.set(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)
    }
}
