package de.bund.digitalservice.useid.events

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.UUID

@Service
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class EventService {
    private val log = KotlinLogging.logger {}

    private val widgets: MutableMap<UUID, SseEmitter> = HashMap()

    /**
     * This method subscribes a widget to events sent to the given id.
     */
    fun subscribeWidget(widgetSessionId: UUID): SseEmitter {
        val sseEmitter = SseEmitter(-1L)
        sseEmitter.onCompletion { unsubscribeWidget(widgetSessionId) }
        widgets[widgetSessionId] = sseEmitter
        log.info { "New widget added with id $widgetSessionId, total widgets: ${widgets.size}" }
        return sseEmitter
    }

    /**
     * This method unsubscribes a widget.
     */
    fun unsubscribeWidget(widgetSessionId: UUID) {
        widgets.remove(widgetSessionId)
        log.info { "Widget with id $widgetSessionId removed, total widgets: ${widgets.size}" }
    }

    /**
     * This method publishes the given event to the according widget. The widget id is specified in the event.
     */
    fun publish(data: Any, type: EventType, widgetSessionId: UUID) {
        log.info { "Publish event to widget $widgetSessionId" }
        val emitter = widgets[widgetSessionId] ?: throw WidgetNotFoundException(widgetSessionId)

        // TODO: Clarify the implications of executing this synchronously on the main thread
        try {
            emitter.send(SseEmitter.event().data(data).name(type.eventName))
            emitter.complete()
        } catch (e: IOException) {
            throw WidgetNotFoundException(widgetSessionId)
        }
    }
}
