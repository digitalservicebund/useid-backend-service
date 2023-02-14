package de.bund.digitalservice.useid.events

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID
import java.util.concurrent.Executors

@Service
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class EventService {
    private val log = KotlinLogging.logger {}

    private val consumers: MutableMap<UUID, SseEmitter> = HashMap()

    private val nonBlockingService = Executors.newCachedThreadPool() // TODO: set size of thread pool if necessary

    /**
     * This method subscribes a consumer to events sent to the given id.
     */
    fun subscribeConsumer(widgetSessionId: UUID): SseEmitter {
        val sseEmitter = SseEmitter()
        sseEmitter.onCompletion { unsubscribeConsumer(widgetSessionId) }
        consumers[widgetSessionId] = sseEmitter
        log.info { "New consumer added with id $widgetSessionId, total consumers: ${consumers.size}" }
        return sseEmitter
    }

    /**
     * This method unsubscribes a consumer.
     */
    fun unsubscribeConsumer(widgetSessionId: UUID) {
        consumers.remove(widgetSessionId)
        log.info { "Consumer with id $widgetSessionId removed, total consumers: ${consumers.size}" }
    }

    /**
     * This method publishes the given event to the according consumer. The consumer id is specified in the event.
     */
    fun publish(event: ServerSentEvent<Any>, widgetSessionId: UUID) {
        log.info { "Publish event to consumer $widgetSessionId" }
        val emitter = consumers[widgetSessionId] ?: throw ConsumerNotFoundException(widgetSessionId)

        nonBlockingService.execute {
            try {
                emitter.send(event)
                emitter.complete()
            } catch (ex: Exception) {
                emitter.completeWithError(ex)
            }
        }
    }

    fun numberConsumers(): Int {
        return consumers.size
    }
}
