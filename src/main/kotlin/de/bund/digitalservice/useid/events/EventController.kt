package de.bund.digitalservice.useid.events

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
@Timed
@Tag(name = "Events", description = "Each widget subscribes to events and thereby gets informed about progress of the identification flow by the eID-Client.")
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class EventController(eventService: EventService) {
    private val log = KotlinLogging.logger {}

    private val eventService: EventService

    init {
        this.eventService = eventService
    }

    /**
     * Receive events from the eID client (i.e. Ident-App) and publish them to the respective consumer.
     */
    fun publishEvent(event: ServerSentEvent<Any>, widgetSessionId: UUID): Mono<ResponseEntity<Nothing>> {
        return Mono.fromCallable { eventService.publish(event, widgetSessionId) }
            .map { ResponseEntity.status(HttpStatus.ACCEPTED).body(null) }
            .doOnError { log.error(it.message) }
            .onErrorReturn(
                ConsumerNotFoundException::class.java,
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
            )
    }

    @PostMapping("/events/{widgetSessionId}/success")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Push SSE to corresponding widget having a success value")
    @ApiResponse(responseCode = "202", content = [Content()])
    @ApiResponse(responseCode = "404", description = "No consumer found for that widgetSessionId", content = [Content()])
    fun sendSuccess(@PathVariable widgetSessionId: UUID, @RequestBody event: SuccessEvent): Mono<ResponseEntity<Nothing>> {
        log.info { "Received success event for consumer: $widgetSessionId" }
        return publishEvent(createServerSentEvent(event), widgetSessionId)
    }

    @PostMapping("/events/{widgetSessionId}/error")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Push SSE to corresponding widget having an error value")
    @ApiResponse(responseCode = "202", content = [Content()])
    @ApiResponse(responseCode = "404", description = "No consumer found for that widgetSessionId", content = [Content()])
    fun sendError(@PathVariable widgetSessionId: UUID, @RequestBody event: ErrorEvent): Mono<ResponseEntity<Nothing>> {
        log.info { "Received event for consumer: $widgetSessionId" }
        return publishEvent(createServerSentEvent(event), widgetSessionId)
    }

    /**
     * At this endpoint consumers can open an SSE channel to consume events.
     */
    @CrossOrigin
    @GetMapping(path = ["/events/{widgetSessionId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @Operation(summary = "Subscribe for receiving SSE for the provided widgetSessionId")
    @ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)])
    fun subscribe(@PathVariable widgetSessionId: UUID): SseEmitter {
        return eventService.subscribeConsumer(widgetSessionId)
    }

    private fun createServerSentEvent(event: SuccessEvent) = ServerSentEvent.builder<Any>()
        .data(event)
        .event(EventType.SUCCESS.eventName)
        .build()

    private fun createServerSentEvent(event: ErrorEvent) = ServerSentEvent.builder<Any>()
        .data(event)
        .event(EventType.ERROR.eventName)
        .build()
}
