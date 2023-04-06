package de.bund.digitalservice.useid.eventstreams

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.UUID

@Service
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class EventStreamService {
    private val log = KotlinLogging.logger {}

    private val eventStreams: MutableMap<UUID, SseEmitter> = HashMap()

    /**
     * This method subscribes to events sent to the given id.
     */
    fun subscribe(eventStreamId: UUID): SseEmitter {
        val sseEmitter = SseEmitter(-1L)
        sseEmitter.onCompletion { unsubscribe(eventStreamId) }
        eventStreams[eventStreamId] = sseEmitter
        log.info { "New subscriber added to event stream with id $eventStreamId, total streams: ${eventStreams.size}" }
        return sseEmitter
    }

    /**
     * This method unsubscribes from an event stream.
     */
    fun unsubscribe(eventStreamId: UUID) {
        eventStreams.remove(eventStreamId)
        log.info { "Event stream with id $eventStreamId removed, total streams: ${eventStreams.size}" }
    }

    /**
     * This method publishes the given event to the according event stream.
     */
    fun publish(data: Any, type: EventType, eventStreamId: UUID) {
        log.info { "Publish event to event stream $eventStreamId" }
        val emitter = eventStreams[eventStreamId] ?: throw EventStreamNotFoundException(eventStreamId)

        // TODO: Clarify the implications of executing this synchronously on the main thread
        try {
            emitter.send(SseEmitter.event().data(data).name(type.eventName))
            // TODO only complete when needed
            // emitter.complete()
        } catch (e: IOException) {
            throw EventStreamNotFoundException(eventStreamId)
        }
    }
}
