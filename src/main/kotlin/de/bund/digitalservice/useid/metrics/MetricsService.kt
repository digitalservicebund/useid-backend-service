package de.bund.digitalservice.useid.metrics

import de.bund.digitalservice.useid.tenant.PARAM_NAME_TENANT_ID
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import org.springframework.stereotype.Service

@Service
class MetricsService {

    fun incrementSuccessCounter(method: String, tenantId: String) {
        createCounter(method, "200", tenantId).increment()
    }

    fun incrementErrorCounter(method: String, tenantId: String) {
        createCounter(method, "500", tenantId).increment()
    }

    private fun createCounter(method: String, status: String, tenantId: String): Counter {
        return Metrics.counter(
            METRIC_NAME_EID_SERVICE_REQUESTS,
            "method",
            method,
            "status",
            status,
            PARAM_NAME_TENANT_ID,
            tenantId,
        )
    }
}
