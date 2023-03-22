package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.apikeys.ApiKeyAuthenticationToken
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

@Tag("test")
internal class TenantIdFilterTest {

    // TODO: get a tenant from tenantProperties itself instead of mocking
    private val tenantProperties: TenantProperties = mockk()
    private val filter = TenantIdFilter(tenantProperties)

    @BeforeEach
    fun beforeEach() {
    }

    @AfterAll
    fun afterAll() {
        unmockkAll()
    }

    @Test
    fun `should assign the tenant id unknown for all unknown calls`() {
        // Given
        val request = MockHttpServletRequest()
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)

        // When
        filter.doFilter(request, response, filterChain)
        assertEquals("unknown", request.getAttribute("tenantId"))
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `should assign the tenant id based on the hostname for calls to the widget`() {
        val tenant = Tenant().apply {
            id = "some-tenant-id"
        }
        every { tenantProperties.findByAllowedHost("foo") } returns tenant
        // Given
        val request = MockHttpServletRequest()
        request.servletPath = "/widget"
        request.addParameter("hostname", "foo")
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)

        // When
        filter.doFilter(request, response, filterChain)
        assertEquals("some-tenant-id", request.getAttribute("tenantId"))

        verify {
            filterChain.doFilter(request, response)
            tenantProperties.findByAllowedHost("foo")
        }
    }

    @Test
    fun `should assign the tenant id based on a valid tenant id query param`() {
        val tenant = Tenant().apply {
            id = "some-tenant-id"
        }
        every { tenantProperties.findByTenantId("some-tenant-id") } returns tenant
        // Given
        val request = MockHttpServletRequest()
        request.servletPath = "/eid-Client"
        request.addParameter("tenant_id", "some-tenant-id")
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)

        // When
        filter.doFilter(request, response, filterChain)
        assertEquals("some-tenant-id", request.getAttribute("tenantId"))

        verify {
            filterChain.doFilter(request, response)
            tenantProperties.findByTenantId("some-tenant-id")
        }
    }

    @Test
    fun `should assign the tenant id of authenticated api calls`() {
        val authentication = ApiKeyAuthenticationToken("foobar", "address", emptyList(), true)
        val securityContext: SecurityContext = mockk()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)

        // Given
        val request = MockHttpServletRequest()
        request.servletPath = "/eid-Client"
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)

        // When
        filter.doFilter(request, response, filterChain)
        val tenant = request.getAttribute("tenant") as Tenant?
        assertEquals("tenant_foo", tenant?.id)

        verify {
            filterChain.doFilter(request, response)
            tenantProperties.findByApiKey("foobar")
        }

        SecurityContextHolder.clearContext()
    }
}
