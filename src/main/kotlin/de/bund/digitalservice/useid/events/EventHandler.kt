package de.bund.digitalservice.useid.events

import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.function.Consumer


@Service
@Slf4j
class EventHandler { // TODO write tests
    private val log: Logger = LoggerFactory.getLogger(EventHandler::class.java) // TODO make this implicit with lombok

    private val consumers: MutableMap<String, Consumer<Event>> = HashMap()

    /**
     * This method subscribes a consumer to events sent to the given id.
     */
    fun subscribeConsumer(id: String, consumer: Consumer<Event>) {
        consumers[id] = consumer
        log.info("New consumer added with id {}, total consumers: {}", id, consumers.size)
    }

    /**
     * This method publishes the given event to the according consumer. The consumer id is specified in the event.
     */
    fun publish(event: Event?) {
        val consumerId = event!!.consumerId
        log.info("Publish event to consumer {}: {}", consumerId, event.toString())
        consumers[consumerId]!!.accept(event)
    }
}