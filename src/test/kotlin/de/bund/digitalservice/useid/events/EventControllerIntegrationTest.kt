package de.bund.digitalservice.useid.events

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI
import java.util.UUID

private val WIDGET_SESSION_ID: UUID = UUID.randomUUID()

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
internal class EventControllerIntegrationTest(
    @Autowired
    @Value("\${local.server.port}")
    val port: Int,
    @Autowired val webTestClient: WebTestClient,
) {

    lateinit var webClient: WebClient

    @BeforeEach
    fun setup() {
        webClient = WebClient.builder()
            .clientConnector(ReactorClientHttpConnector())
            .codecs { it.defaultCodecs() }
            .exchangeStrategies(ExchangeStrategies.withDefaults())
            .baseUrl("http://localhost:$port/api/v1")
            .build()
    }

    // TODO: write tests for happy case (success and error case)

    @Test
    fun `publish success event returns 404 if session id is unknown`() {
        // Given
        val unknownId = UUID.randomUUID()
        val event = successEvent()

        // When
        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/events/$unknownId/success"))
            .bodyValue(event)
            .exchange()
            // Then
            .expectStatus().isNotFound
            .expectBody().isEmpty
    }

    @Test
    @Disabled("Failing due to race condition.") // TODO: enable and fix test
    fun `publish success event returns 404 if client disconnected`() {
        // Given
        val event = successEvent()

        val disposable = webClient.get().uri("/events/$WIDGET_SESSION_ID")
            .accept(TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(SuccessEvent::class.java)
            .subscribe()

        // Then
        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/events/$WIDGET_SESSION_ID/success"))
            .bodyValue(event)
            .exchange()
            .expectStatus().isAccepted

        // When
        disposable.dispose()

        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/events/$WIDGET_SESSION_ID/success"))
            .bodyValue(event)
            .exchange()
            .expectStatus().isNotFound
            .expectBody().isEmpty
    }

    @Test
    fun `publish success event returns 415 if the request body doesn't contain json `() {
        // Given
        val event = "some-malformed-event"

        // When
        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/events/${UUID.randomUUID()}/success"))
            .bodyValue(event)
            .exchange()
            // Then
            .expectStatus().isEqualTo(415)
    }

    @Test
    fun `publish success event returns 400 if the event is malformed `() {
        // Given
        val event = MalformedEventWithWrongAttributes("some-string")

        // When
        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/events/${UUID.randomUUID()}/success"))
            .bodyValue(event)
            .exchange()
            // Then
            .expectStatus().isBadRequest
    }

    @Test
    fun `publish success event returns 400 if the event id is not a valid UUID `() {
        // Given
        val malformedEventId = "some-id-string"

        // When
        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/events/$malformedEventId/success"))
            .bodyValue(successEvent())
            .exchange()
            // Then
            .expectStatus().isBadRequest
    }

    private fun successEvent(): SuccessEvent {
        return SuccessEvent("some-refresh-address")
    }

    private fun errorEvent(): ErrorEvent {
        return ErrorEvent("some error happened")
    }

    data class MalformedEventWithWrongAttributes(val something: String)
}
