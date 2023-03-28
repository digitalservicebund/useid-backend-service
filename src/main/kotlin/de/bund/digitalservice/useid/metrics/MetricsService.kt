package de.bund.digitalservice.useid.metrics

import de.bund.digitalservice.useid.tenant.PARAM_NAME_TENANT_ID
import io.micrometer.core.instrument.Metrics
import org.springframework.stereotype.Service

@Service
class MetricsService {
    fun incrementCounter(method: String, status: String, tenantId: String) {
        Metrics.counter(
            METRIC_NAME_EID_SERVICE_REQUESTS,
            "method",
            method,
            "status",
            status,
            PARAM_NAME_TENANT_ID,
            tenantId,
        ).increment()
    }
}
