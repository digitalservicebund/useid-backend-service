package de.bund.digitalservice.useid.refresh

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.identification.IdentificationSession
import de.bund.digitalservice.useid.identification.IdentificationSessionService
import io.mockk.every
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.DefaultUriBuilderFactory
import java.util.*
import java.util.stream.Stream

@Tag("test")
@ExtendWith(value = [SpringExtension::class])
@WebMvcTest(controllers = [RefreshController::class])
@Import(value = [ApplicationProperties::class])
@WithMockUser
internal class RefreshControllerTest(@Autowired val webTestClient: WebTestClient) {
    @MockkBean
    private lateinit var identificationSessionService: IdentificationSessionService

    private val client = clientWithoutParameterEncoding()

    @ParameterizedTest
    @MethodSource("refreshParameters")
    fun `refresh endpoint escapes forwarded query parameters`(inputParameterString: String, expectedParameterString: String) {
        // Given
        val eIdSessionId: UUID = UUID.randomUUID()
        val refreshAddress = "some-refresh-address"
        val identificationSession = IdentificationSession(UUID.randomUUID(), refreshAddress, emptyList())
        every { identificationSessionService.findByEIdSessionIdOrThrow(any()) } returns identificationSession

        // When
        client
            .get()
            .uri("/refresh?sessionId=$eIdSessionId&$inputParameterString")
            .exchange()
            // Then
            .expectStatus()
            .is3xxRedirection
            .expectHeader()
            .location("$refreshAddress?sessionId=$eIdSessionId&$expectedParameterString")
    }
    private fun refreshParameters(): Stream<Arguments?>? {
        return Stream.of(
            Arguments.of("foo=alert(1)", "foo=alert%281%29"), // encode unencoded value
            Arguments.of("alert(1)=foo", "alert%281%29=foo"), // encode unencoded key
            Arguments.of("foo=alert%281%29", "foo=alert%281%29"), // keep value encoding
            Arguments.of("foo=bar%26bar%3Dfoo", "foo=bar%26bar%3Dfoo"), // keep value encoding
            Arguments.of("alert%281%29=foo", "alert%281%29=foo"), // keep key encoding
        )
    }

    private fun clientWithoutParameterEncoding(): WebTestClient {
        val defaultUriBuilderFactory = DefaultUriBuilderFactory()
        defaultUriBuilderFactory.encodingMode = DefaultUriBuilderFactory.EncodingMode.NONE
        return webTestClient.mutate().uriBuilderFactory(defaultUriBuilderFactory).build()
    }
}
