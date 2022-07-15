package de.bund.digitalservice.useid.events

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
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
@Tag("integration")
internal class EventControllerTest(
    @Autowired @Value("\${local.server.port}")
    val port: Int,
    @Autowired val webTestClient: WebTestClient
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

    @Test
    fun `publish and receive event happy case`() {
        val event = event(WIDGET_SESSION_ID)

        publishEvent(event)

        val result = webTestClient
            .get()
            .uri(URI.create("http://localhost:$port/api/v1/events/$WIDGET_SESSION_ID"))
            .accept(TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(TEXT_EVENT_STREAM)
            .returnResult(Event::class.java)

        StepVerifier.create(result.responseBody)
            .expectNext(event)
            .thenCancel()
            .log()
            .verify()
    }

    @Test
    fun `published event is not consumed if sent to different consumer `() {
        // Given
        val event = event(UUID.randomUUID())

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
        publishEvent(event)

        // Then
        verifier.verify(ofSeconds(1))
    }

    @Test
    fun `publish event returns 404 if session id is unknown `() {
        // Given
        val unknownId = UUID.randomUUID()
        val event = event(unknownId)

        // When
        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/events"))
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
            .uri(URI.create("http://localhost:$port/api/v1/events"))
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
            .uri(URI.create("http://localhost:$port/api/v1/events"))
            .bodyValue(event)
            .exchange()
            // Then
            .expectStatus().isBadRequest
    }

    @Test
    fun `publish event returns 400 if the event id is not a valid UUID `() {
        // Given
        val event = MalformedEventWithoutUUID("some-id-string", "some-refresh-address")

        // When
        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/events"))
            .bodyValue(event)
            .exchange()
            // Then
            .expectStatus().isBadRequest
    }

    private fun publishEvent(event: Event) {
        webClient.post().uri("/events")
            .contentType(APPLICATION_JSON)
            .bodyValue(event)
            .retrieve()
            .bodyToMono<ResponseEntity<Nothing>>()
            .subscribe()
    }

    private fun event(widgetSessionId: UUID) = Event(widgetSessionId, "some-refresh-address")

    data class MalformedEventWithWrongAttributes(val something: String)
    data class MalformedEventWithoutUUID(val widgetSessionId: String, val encryptedRefreshAddress: String)
}
