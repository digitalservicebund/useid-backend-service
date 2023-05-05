package de.bund.digitalservice.useid.tenant

import de.bund.digitalservice.useid.metrics.METRICS_TAG_NAME_TENANT_ID
import io.micrometer.common.KeyValue
import io.micrometer.common.KeyValues
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention
import org.springframework.http.server.observation.ServerRequestObservationContext
import org.springframework.stereotype.Component

@Component
class RequestMetricsTenantTagProvider : DefaultServerRequestObservationConvention() {

    override fun getLowCardinalityKeyValues(context: ServerRequestObservationContext): KeyValues {
        return super.getLowCardinalityKeyValues(context).and(tenantIdTag(context))
    }

    private fun tenantIdTag(context: ServerRequestObservationContext): KeyValues {

        val tenant: Tenant? = context.carrier.getAttribute(REQUEST_ATTR_TENANT) as Tenant?
        tenant?.let {
            return KeyValues.of(KeyValue.of(METRICS_TAG_NAME_TENANT_ID, tenant.id))
        }

        return KeyValues.empty()
    }
}
