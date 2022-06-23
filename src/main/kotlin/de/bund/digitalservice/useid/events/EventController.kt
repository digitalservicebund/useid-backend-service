package de.bund.digitalservice.useid.events

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/api/v1")
class EventController(eventHandler: EventHandler) { // TODO write tests
    private val log: Logger = LoggerFactory.getLogger(EventController::class.java) // TODO make this implicit with lombok

    private val eventHandler: EventHandler

    init {
        this.eventHandler = eventHandler
    }

    /**
     * This endpoint receives events from the eID client (i.e. Ident-App) and publishes them to the respective consumer.
     */
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    fun send(@RequestBody event: Event?): Mono<Event> {
        log.info("Received event: '{}'", event.toString())
        eventHandler.publish(event)
        return Mono.just(event!!)
    }

    /**
     * At this endpoint, consumers can open an SSE channel to consume events.
     */
    @CrossOrigin
    @GetMapping(path = ["/events/{consumerId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun consumer(@PathVariable consumerId: String): Flux<ServerSentEvent<Any>>? {
        return Flux.create { sink: FluxSink<Any?> -> eventHandler.subscribeConsumer(consumerId) { t: Event? -> sink.next(t!!) } }
                .map { data: Any? -> createServerSentEvent(data) } // TODO make data type explicit
    }

    private fun createServerSentEvent(data: Any?) = ServerSentEvent.builder<Any>()
            .data(data) // TODO return encrypted refreshURL and widgetSessionId
            .event("success") // TODO make this an enum
            .build()
}