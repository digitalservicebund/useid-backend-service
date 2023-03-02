package de.bund.digitalservice.useid.config

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
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletResponse

@Tag("test")
internal class TenantIdFilterTest {

    private val contentSecurityPolicyProperties: ContentSecurityPolicyProperties = mockk()
    private val filter = TenantIdFilter(contentSecurityPolicyProperties)

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
        every { contentSecurityPolicyProperties.getTenantId("foo") } returns "some-tenant-id"
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
            contentSecurityPolicyProperties.getTenantId("foo")
        }
    }
}
