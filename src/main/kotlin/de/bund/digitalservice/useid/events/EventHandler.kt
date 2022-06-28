package de.bund.digitalservice.useid.events

import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
class EventHandler {
    private val log = KotlinLogging.logger {}

    private val consumers: MutableMap<String, Consumer<Event>> = HashMap()

    /**
     * This method subscribes a consumer to events sent to the given id.
     */
    fun subscribeConsumer(widgetSessionId: String, consumer: Consumer<Event>) {
        consumers[widgetSessionId] = consumer
        log.info { "New consumer added with id $widgetSessionId, total consumers: ${consumers.size}" }
    }

    /**
     * This method publishes the given event to the according consumer. The consumer id is specified in the event.
     */
    fun publish(event: Event) {
        val widgetSessionId = event.widgetSessionId
        log.info { "Publish event to consumer $widgetSessionId" }
        consumers[widgetSessionId]!!.accept(event)
    }

    fun numberConsumers(): Int {
        return consumers.size
    }
}
