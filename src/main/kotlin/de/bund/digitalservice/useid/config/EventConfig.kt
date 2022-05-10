package de.bund.digitalservice.useid.config

import org.springframework.context.ApplicationEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.Many
import java.time.Duration

@Configuration
class EventConfig {
    @Bean
    fun eventNotifications(): Many<SuccessEvent>? {
        return Sinks.many().replay().limit(Duration.ofSeconds(3))
    }

    class SuccessEvent(source: Any?) : ApplicationEvent(source!!)
}
