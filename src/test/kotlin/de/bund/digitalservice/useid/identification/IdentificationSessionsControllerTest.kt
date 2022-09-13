package de.bund.digitalservice.useid.identification

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

private const val AUTHORIZATION_HEADER = "Bearer some-api-key"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("test")
@ExtendWith(OutputCaptureExtension::class)
class IdentificationSessionsControllerTest(@Autowired val webTestClient: WebTestClient) {

    @MockkBean
    private lateinit var identificationSessionService: IdentificationSessionService

    @Test
    fun `identificationSessionService create method should log if error was thrown`(output: CapturedOutput) {
        every { identificationSessionService.create(any(), any()) } returns Mono.fromCallable {
            throw Error("log this!")
            mockk<IdentificationSession>()
        }

        webTestClient
            .post()
            .uri("/api/v1/identification/sessions")
            .headers { it.set(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER) }
            .exchange()
            .expectStatus()
            .is5xxServerError

        assertThat(output.all, containsString("log this!"))
    }
}
