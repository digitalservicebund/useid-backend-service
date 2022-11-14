package de.bund.digitalservice.useid.config

import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

internal const val METRIC_NAME_EID_SERVICE_REQUESTS = "eid_service.requests"

@Configuration
class MetricsConfig {
    @Bean
    fun timedAspect(registry: MeterRegistry): TimedAspect {
        return TimedAspect(registry)
    }
}
