package de.bund.digitalservice.useid.events

import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.function.Consumer


@Service
class EventHandler { // TODO write tests
    private val log = KotlinLogging.logger {}

    private val consumers: MutableMap<String, Consumer<Event>> = HashMap()

    /**
     * This method subscribes a consumer to events sent to the given id.
     */
    fun subscribeConsumer(id: String, consumer: Consumer<Event>) {
        consumers[id] = consumer
        log.info { "New consumer added with id $id, total consumers: ${consumers.size}" }
    }

    /**
     * This method publishes the given event to the according consumer. The consumer id is specified in the event.
     */
    fun publish(event: Event) {
        val consumerId = event.consumerId!!
        log.info { "Publish event to consumer $consumerId" }
        consumers[consumerId]!!.accept(event)
    }
}