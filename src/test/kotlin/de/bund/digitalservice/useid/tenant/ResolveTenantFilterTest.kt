package de.bund.digitalservice.useid.tenant

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest

@Tag("test")
internal class ResolveTenantFilterTest {

    private val tenantProperties: TenantProperties = mockk()
    private val filter = ResolveTenantFilter(tenantProperties)

    private val validTenant: Tenant = Tenant().apply {
        id = "integration_test_1"
        allowedHosts = listOf("i.am.allowed.de")
    }

    @AfterAll
    fun afterAll() {
        unmockkAll()
    }

    @Test
    fun `should not assign any tenant for all unknown calls`() {
        // Given
        val request = MockHttpServletRequest()
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        assertEquals(null, request.getAttribute(REQUEST_ATTR_TENANT))
        verify {
            filterChain.doFilter(request, response)
        }
    }

    @Test
    fun `should assign the correct tenant based on the corresponding hostname for calls to the widget`() {
        // Given
        every { tenantProperties.findByAllowedHost(any()) } returns validTenant
        val request = MockHttpServletRequest()
        request.servletPath = "/widget"
        request.addParameter("hostname", validTenant.allowedHosts[0])
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        assertEquals(validTenant.id, getTenantIdFromRequest(request))
        verify {
            filterChain.doFilter(request, response)
            tenantProperties.findByAllowedHost(validTenant.allowedHosts[0])
        }
    }

    @Test
    fun `should assign the correct tenant based on the corresponding tenant id query param`() {
        // Given
        every { tenantProperties.findByTenantId(any()) } returns validTenant
        val request = MockHttpServletRequest()
        request.addParameter(PARAM_NAME_TENANT_ID, validTenant.id)
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        assertEquals(validTenant.id, getTenantIdFromRequest(request))
        verify {
            filterChain.doFilter(request, response)
            tenantProperties.findByTenantId(any())
        }
    }

    @Test
    fun `should not assign any tenant when findByTenantId returns null`() {
        // Given
        every { tenantProperties.findByTenantId(any()) } returns null
        val request = MockHttpServletRequest()
        request.addParameter(PARAM_NAME_TENANT_ID, "i-am-not-a-valid-tenant-id")
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        assertEquals(null, request.getAttribute(REQUEST_ATTR_TENANT))
        verify {
            filterChain.doFilter(request, response)
            tenantProperties.findByTenantId(any())
        }
    }

    private fun getTenantIdFromRequest(request: MockHttpServletRequest) = (request.getAttribute(REQUEST_ATTR_TENANT) as Tenant).id
}
