package de.bund.digitalservice.useid.events

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
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
    fun `publish and receive event happy case`() {
        // Given
        val event = event()

        val verifier = webClient.get().uri("/events/$WIDGET_SESSION_ID")
            .accept(TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(Event::class.java)
            .log()
            .`as` { StepVerifier.create(it) }
            .consumeNextWith {
                // Then
                // TODO:
                // assertEquals(event.success, it.success)
            }
            .thenCancel()
            .verifyLater()

        // When
        publishEvent(event, WIDGET_SESSION_ID)

        verifier.verify()
    }

    @Test
    fun `published event is not consumed if sent to different consumer `() {
        // Given
        val verifier = webClient.get().uri("/events/$WIDGET_SESSION_ID")
            .accept(TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(Event::class.java)
            .log()
            .`as` { StepVerifier.create(it) }
            .expectNextCount(0)
            .thenCancel()
            .verifyLater()

        // When
        publishEvent(event(), UUID.randomUUID())

        // Then
        verifier.verify(ofSeconds(1))
    }

    @Test
    fun `publish event returns 404 if session id is unknown `() {
        // Given
        val unknownId = UUID.randomUUID()
        val event = event()

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
    fun `publish event returns 415 if the request body doesn't contain json `() {
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
    fun `publish event returns 400 if the event is malformed `() {
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
    fun `publish event returns 400 if the event id is not a valid UUID `() {
        // Given
        val malformedEventId = "some-id-string"

        // When
        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/events/${malformedEventId}/success"))
            .bodyValue(event())
            .exchange()
            // Then
            .expectStatus().isBadRequest
    }

    private fun publishEvent(event: Event, widgetSessionId: UUID) {
        webClient.post().uri("/events/$widgetSessionId/${if (event.success) "success" else "error"}")
            .contentType(APPLICATION_JSON)
            .bodyValue(event)
            .retrieve()
            .bodyToMono<ResponseEntity<Nothing>>()
            .subscribe()
    }

    private fun event(success: Boolean = true): Event {
        return if (success) {
            SuccessEvent("some-refresh-address")
        } else {
            ErrorEvent("some error happened")
        }
    }

    data class MalformedEventWithWrongAttributes(val something: String)
}
