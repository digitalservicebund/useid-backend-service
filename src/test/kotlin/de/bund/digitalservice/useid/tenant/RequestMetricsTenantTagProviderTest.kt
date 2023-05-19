package de.bund.digitalservice.useid.tenant

import de.bund.digitalservice.useid.metrics.METRICS_TAG_NAME_TENANT_ID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.server.observation.ServerRequestObservationContext
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

@Tag("test")
class RequestMetricsTenantTagProviderTest {

    private val requestMetricsTenantTagProvider = RequestMetricsTenantTagProvider()
    private val tenant = Tenant().apply {
        id = "foobar"
    }

    @Test
    fun `tenantIdTag should return tenant id keyValue pair if given context contains tenant as request attribute`() {
        // Given
        val mockHttpRequest = MockHttpServletRequest()
        mockHttpRequest.setAttribute(REQUEST_ATTR_TENANT, tenant)

        val mockHttpServletResponse = MockHttpServletResponse()

        val context = ServerRequestObservationContext(
            mockHttpRequest,
            mockHttpServletResponse,
        )

        // When
        val result = requestMetricsTenantTagProvider.getLowCardinalityKeyValues(context)

        // Then
        assertThat(result.find { it.key == METRICS_TAG_NAME_TENANT_ID }?.value).isEqualTo(tenant.id)
    }

    @Test
    fun `tenantIdTag should return empty keyValue pair if given context does not contain tenant as request attribute`() {
        // Given
        val mockHttpRequest = MockHttpServletRequest()
        val mockHttpServletResponse = MockHttpServletResponse()

        val context = ServerRequestObservationContext(
            mockHttpRequest,
            mockHttpServletResponse,
        )

        // When
        val result = requestMetricsTenantTagProvider.getLowCardinalityKeyValues(context)

        // Then
        assertThat(result.find { it.key == METRICS_TAG_NAME_TENANT_ID }?.value).isNull()
    }
}
