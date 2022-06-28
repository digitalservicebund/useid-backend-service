package de.bund.digitalservice.useid.events

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier
import java.time.Duration.ofSeconds

private const val CONSUMER_ID = "some-id"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
internal class EventControllerTest(
    @Autowired @Value("\${local.server.port}") val port: Int
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
        // Given
        val event = event(CONSUMER_ID)

        val verifier = webClient.get().uri("/events/$CONSUMER_ID")
            .accept(TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(Event::class.java)
            .log()
            .`as` { StepVerifier.create(it) }
            .consumeNextWith {
                // Then
                assertEquals(event, it)
            }
            .thenCancel()
            .verifyLater()

        // When
        webClient.post().uri("/events")
            .contentType(APPLICATION_JSON)
            .bodyValue(event)
            .exchange()
            .then()
            .block()

        verifier.verify()
    }

    @Test
    fun `publish event to different consumer `() {
        // Given
        val event = event("some-other-consumer")

        val verifier = webClient.get().uri("/events/$CONSUMER_ID")
            .accept(TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(Event::class.java)
            .log()
            .`as` { StepVerifier.create(it) }
            .expectNextCount(0)
            .thenCancel()
            .verifyLater()

        // When
        webClient.post().uri("/events")
            .contentType(APPLICATION_JSON)
            .bodyValue(event)
            .exchange()
            .then()
            .block()

        // Then
        verifier.verify(ofSeconds(1))
    }

    private fun event(consumerId: String) = Event(consumerId, "some-refresh-address", "some-widget-id")
}
