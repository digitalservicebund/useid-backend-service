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
import org.springframework.test.web.reactive.server.FluxExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.body
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

private const val CONSUMER_ID = "some-id"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
internal class EventControllerTest(
        @Autowired val eventController: EventController,
        @Autowired @Value("\${local.server.port}") val port: Int
) {

    lateinit var webTestClient: WebTestClient
    lateinit var webClient: WebClient

    @BeforeEach
    fun setup() {
        webClient = WebClient.builder()
                .clientConnector(ReactorClientHttpConnector())
                .codecs { it.defaultCodecs() }
                .exchangeStrategies(ExchangeStrategies.withDefaults())
                .baseUrl("http://localhost:$port/api/v1/events")
                .build()

        webTestClient = WebTestClient.bindToController(eventController)
                .configureClient()
                .baseUrl("/api/v1/events")
                .build()
    }


    @Test
    fun `receive event happy case`() {
        // Given
        val event = event(CONSUMER_ID);

        val verifier = webClient.get().uri("/$CONSUMER_ID")
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
        webClient.post().uri("")
                .contentType(APPLICATION_JSON)
                .bodyValue(event)
                .exchange()
                .then()
                .block()

        verifier.verify()
    }

    @Test
    fun `receive event happy case 2`() {
        // Given
        val event = event(CONSUMER_ID)

        val result: FluxExchangeResult<Event> = webTestClient.get().uri("/$CONSUMER_ID")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk
                .returnResult()

        val verifyLater = StepVerifier.create(result.responseBody)
                .expectNext(event)
                .expectNextCount(1)
                .consumeNextWith { e ->
                    // Then
                    assertEquals(event, e)
                }
                .thenCancel()
                .verifyLater()

        // When
        webTestClient.post().uri("/api/v1/events")
                .contentType(APPLICATION_JSON)
                .body(Mono.just(event))
                .exchange()
                .expectStatus().isOk

        verifyLater.verify()
    }

    private fun event(consumerId: String) = Event(consumerId, "some-refresh-address", "some-widget-id")
}
