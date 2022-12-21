package de.bund.digitalservice.useid.events

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.http.ResponseEntity
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.test.StepVerifier
import java.net.URI
import java.time.Duration.ofSeconds
import java.util.UUID

private val WIDGET_SESSION_ID: UUID = UUID.randomUUID()

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class EventControllerIntegrationTest(
    @Autowired
    @Value("\${local.server.port}")
    val port: Int,
    @Autowired val webTestClient: WebTestClient
) : PostgresTestcontainerIntegrationTest() {

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

    @Test
    fun `publish and receive success event happy case`() {
        // Given
        val event = successEvent()

        val verifier = webClient.get().uri("/events/$WIDGET_SESSION_ID")
            .accept(TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(SuccessEvent::class.java)
            .log()
            .`as` { StepVerifier.create(it) }
            .consumeNextWith {
                // Then
                assertEquals(event, it)
            }
            .thenCancel()
            .verifyLater()

        // When
        publishEvent(event, WIDGET_SESSION_ID)

        verifier.verify()
    }

    @Test
    fun `publish and receive error event happy case`() {
        // Given
        val event = errorEvent()

        val verifier = webClient.get().uri("/events/$WIDGET_SESSION_ID")
            .accept(TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(ErrorEvent::class.java)
            .log()
            .`as` { StepVerifier.create(it) }
            .consumeNextWith {
                // Then
                assertEquals(event, it)
            }
            .thenCancel()
            .verifyLater()

        // When
        publishEvent(event, WIDGET_SESSION_ID)

        verifier.verify()
    }

    @Test
    fun `published success event is not consumed if sent to different consumer `() {
        // Given
        val verifier = webClient.get().uri("/events/$WIDGET_SESSION_ID")
            .accept(TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(SuccessEvent::class.java)
            .log()
            .`as` { StepVerifier.create(it) }
            .expectNextCount(0)
            .thenCancel()
            .verifyLater()

        // When
        publishEvent(successEvent(), UUID.randomUUID())

        // Then
        verifier.verify(ofSeconds(1))
    }

    @Test
    fun `publish success event returns 404 if session id is unknown `() {
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

    private fun publishEvent(event: Any, widgetSessionId: UUID) {
        webClient.post().uri("/events/$widgetSessionId/${if (event is SuccessEvent) "success" else "error"}")
            .contentType(APPLICATION_JSON)
            .bodyValue(event)
            .retrieve()
            .bodyToMono<ResponseEntity<Nothing>>()
            .subscribe()
    }

    private fun successEvent(): SuccessEvent {
        return SuccessEvent("some-refresh-address")
    }

    private fun errorEvent(): ErrorEvent {
        return ErrorEvent("some error happened")
    }

    data class MalformedEventWithWrongAttributes(val something: String)
}
