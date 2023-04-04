package de.bund.digitalservice.useid.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import org.springframework.stereotype.Service

internal const val METRICS_TAG_NAME_TENANT_ID = "tenant_id"

@Service
class MetricsService {

    fun incrementSuccessCounter(method: String, tenantId: String) {
        createCounter(method, "200", tenantId).increment()
    }

    fun incrementErrorCounter(method: String, tenantId: String) {
        createCounter(method, "500", tenantId).increment()
    }

    private fun createCounter(method: String, status: String, tenantId: String): Counter {
        // TODO: save counters with the same "method, status, tenantId" and reuse them instead of creating a new one
        return Metrics.counter(
            METRIC_NAME_EID_SERVICE_REQUESTS,
            "method",
            method,
            "status",
            status,
            METRICS_TAG_NAME_TENANT_ID,
            tenantId,
        )
    }
}
