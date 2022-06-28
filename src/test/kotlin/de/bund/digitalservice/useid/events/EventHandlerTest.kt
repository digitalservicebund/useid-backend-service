package de.bund.digitalservice.useid.events

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.function.Consumer

private const val CONSUMER_ID = "some-id"

@Tag("test")
internal class EventHandlerTest {
    private val eventHandler: EventHandler = EventHandler()
    private val consumer = mockk<Consumer<Event>>()

    @Test
    internal fun `subscribe and publish happy path`() {
        // Given
        val event = event(CONSUMER_ID)
        every { consumer.accept(any()) } returns Unit

        // When
        eventHandler.subscribeConsumer(CONSUMER_ID, consumer)

        // Then
        assertEquals(1, eventHandler.numberConsumers())

        // When
        eventHandler.publish(event)

        // Then
        verify { consumer.accept(event) }
    }

    @Test
    internal fun `publish to unknown customer throws exception`() {
        // Given
        val event = event("unknown-consumer-id")
        every { consumer.accept(any()) } returns Unit

        // When
        eventHandler.subscribeConsumer(CONSUMER_ID, consumer)

        // Then
        assertEquals(1, eventHandler.numberConsumers())
        assertThrows<NullPointerException> {
            // When
            eventHandler.publish(event)
        }
        verify(exactly = 0) { consumer.accept(event) }
    }

    private fun event(consumerId: String) = Event(consumerId, "some-refresh-address", "some-widget-id")
}
