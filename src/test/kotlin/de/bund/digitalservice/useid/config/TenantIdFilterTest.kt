package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.apikeys.ApiKeyAuthenticationToken
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletResponse

@Tag("test")
internal class TenantIdFilterTest {

    private val tenantIdProperties: TenantIdProperties = mockk()
    private val filter = TenantIdFilter(tenantIdProperties)

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
        every { tenantIdProperties.getTenantIdForHost("foo") } returns "some-tenant-id"
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
            tenantIdProperties.getTenantIdForHost("foo")
        }
    }

    @Test
    fun `should assign the tenant id based on a valid tenant id query param`() {
        every { tenantIdProperties.getSanitizedTenantID("some-tenant-id") } returns "some-tenant-id"
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
            tenantIdProperties.getSanitizedTenantID("some-tenant-id")
        }
    }

    @Test
    fun `should assign the tenant id of authenticated api calls`() {
        val authentication: ApiKeyAuthenticationToken = ApiKeyAuthenticationToken("key", "address", emptyList(), true, "some-tenant-id")
        val securityContext: SecurityContext = mockk()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)

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
            tenantIdProperties.getSanitizedTenantID("some-tenant-id")
        }

        SecurityContextHolder.clearContext()
    }
}
