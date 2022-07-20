package de.bund.digitalservice.useid.events

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class EventController(eventService: EventService) {
    private val log = KotlinLogging.logger {}

    private val eventService: EventService

    init {
        this.eventService = eventService
    }

    /**
     * This endpoint receives events from the eID client (i.e. Ident-App) and publishes them to the respective consumer.
     */
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun send(@RequestBody event: Event): Mono<ResponseEntity<Nothing>> {
        log.info { "Received event for consumer: ${event.widgetSessionId}" }

        return Mono.fromCallable { eventService.publish(event) }
            .map { ResponseEntity.status(HttpStatus.ACCEPTED).body(null) }
            .doOnError { log.error(it.message) }
            .onErrorReturn(
                ConsumerNotFoundException::class.java,
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
            )
    }

    /**
     * At this endpoint, consumers can open an SSE channel to consume events.
     */
    @CrossOrigin
    @GetMapping(path = ["/events/{widgetSessionId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun consumer(@PathVariable widgetSessionId: UUID): Flux<ServerSentEvent<Any>>? {
        return Flux.create { sink: FluxSink<Event> -> eventService.subscribeConsumer(widgetSessionId) { event: Event -> sink.next(event) } }
            .map { event: Event -> createServerSentEvent(event) }
    }

    private fun createServerSentEvent(event: Event) = ServerSentEvent.builder<Any>()
        .data(event)
        .event(EventType.SUCCESS.eventName)
        .build()
}
