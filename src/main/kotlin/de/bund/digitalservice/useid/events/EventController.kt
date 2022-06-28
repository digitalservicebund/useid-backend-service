package de.bund.digitalservice.useid.events

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/api/v1")
class EventController(eventHandler: EventHandler) {
    private val log = KotlinLogging.logger {}

    private val eventHandler: EventHandler

    init {
        this.eventHandler = eventHandler
    }

    /**
     * This endpoint receives events from the eID client (i.e. Ident-App) and publishes them to the respective consumer.
     */
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    fun send(@RequestBody event: Event): Mono<Event> {
        log.info { "Received event for consumer: ${event.consumerId}" }
        eventHandler.publish(event)
        return Mono.just(event)
    }

    /**
     * At this endpoint, consumers can open an SSE channel to consume events.
     */
    @CrossOrigin
    @GetMapping(path = ["/events/{consumerId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun consumer(@PathVariable consumerId: String): Flux<ServerSentEvent<Any>>? {
        return Flux.create { sink: FluxSink<Event> -> eventHandler.subscribeConsumer(consumerId) { event: Event -> sink.next(event) } }
                .map { event: Event -> createServerSentEvent(event) }
    }

    private fun createServerSentEvent(event: Event) = ServerSentEvent.builder<Any>()
            .data(event)
            .event(EventType.SUCCESS.eventName)
            .build()
}