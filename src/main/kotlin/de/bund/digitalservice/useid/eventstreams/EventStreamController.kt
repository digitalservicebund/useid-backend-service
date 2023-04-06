package de.bund.digitalservice.useid.eventstreams

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
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID

@RestController
@RequestMapping("/api/v1/event-streams")
@Timed
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class EventStreamController(private val eventStreamService: EventStreamService) {
    private val log = KotlinLogging.logger {}

    /**
     * Receive a success event from the eID client (i.e. Ident-App) and publish it to the respective consumer.
     */
    @PostMapping("/{eventStreamId}/success")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Push SSE to corresponding widget having a success value")
    @ApiResponse(responseCode = "202", content = [Content()])
    @ApiResponse(
        responseCode = "404",
        description = "No consumer found for that widgetSessionId",
        content = [Content()],
    )
    @Tag(name = "eID-Client")
    fun sendSuccess(
        @PathVariable eventStreamId: UUID,
        @RequestBody successEvent: SuccessEvent,
    ): ResponseEntity<Nothing> {
        log.info { "Received success event for consumer: $eventStreamId" }
        eventStreamService.publish(successEvent, EventType.SUCCESS, eventStreamId)
        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

    /**
     * Receive an error event from the eID client (i.e. Ident-App) and publish it to the respective consumer.
     */
    @PostMapping("/{eventStreamId}/error")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Push SSE to corresponding widget having an error value")
    @ApiResponse(responseCode = "202", content = [Content()])
    @ApiResponse(
        responseCode = "404",
        description = "No consumer found for that eventStreamId",
        content = [Content()],
    )
    @Tag(name = "eID-Client", description = "Those endpoints are called by the eID-Client, i.e. the BundesIdent app.")
    fun sendError(@PathVariable eventStreamId: UUID, @RequestBody errorEvent: ErrorEvent): ResponseEntity<Nothing> {
        log.info { "Received event for consumer: $eventStreamId" }
        eventStreamService.publish(errorEvent, EventType.ERROR, eventStreamId)
        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

    /**
     * Subscribe to events for a specific widget.
     */
    @CrossOrigin
    @GetMapping(path = ["/{eventStreamId}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @Operation(summary = "The widget subscribes at this endpoint to events to get informed about progress of the identification flow by the eID-Client.")
    @ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE)])
    @Tag(name = "Widget", description = "Those endpoints are called by the web widget included as iframe in the eServer web page.")
    fun subscribe(@PathVariable eventStreamId: UUID): SseEmitter {
        return eventStreamService.subscribe(eventStreamId)
    }
}
