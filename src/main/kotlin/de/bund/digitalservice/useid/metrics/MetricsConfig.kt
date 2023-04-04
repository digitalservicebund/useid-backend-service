package de.bund.digitalservice.useid.metrics

import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

internal const val METRIC_NAME_EID_SERVICE_REQUESTS = "eid_service.requests"
internal const val METRIC_NAME_EID_INFORMATION = "get_eid_information"
internal const val METRIC_NAME_EID_TCTOKEN = "get_tc_token"

@Configuration
class MetricsConfig {
    @Bean
    fun timedAspect(registry: MeterRegistry): TimedAspect {
        return TimedAspect(registry)
    }
}
