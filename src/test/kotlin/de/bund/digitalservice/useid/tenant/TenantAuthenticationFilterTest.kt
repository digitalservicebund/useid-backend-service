package de.bund.digitalservice.useid.tenant

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletResponse
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Tag("test")
class TenantAuthenticationFilterTest {

    private val tenantProperties: TenantProperties = mockk()

    private lateinit var manager: TenantAuthenticationManager
    private lateinit var filter: TenantAuthenticationFilter

    private lateinit var validTenant: Tenant

    @BeforeAll
    fun setup() {
        manager = TenantAuthenticationManager(tenantProperties)
        filter = TenantAuthenticationFilter(manager)
    }

    @BeforeEach
    fun beforeEach() {
        validTenant = Tenant().apply {
            id = "valid-id"
            apiKey = "valid-api-key"
            refreshAddress = "valid-refresh-address"
        }

        mockkStatic(::setAuthentication)
        mockkStatic(::removeAuthentication)
        every { setAuthentication(any()) } returns Unit
        every { removeAuthentication() } returns Unit
    }

    @AfterAll
    fun afterAll() {
        unmockkAll()
    }

    @Test
    fun `should pass request with authentication object if header contains valid api key`() {
        // Given
        val request = MockHttpServletRequest()
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)
        every { tenantProperties.findByApiKey(any()) } returns validTenant
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer ${validTenant.apiKey}")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify {
            setAuthentication(
                withArg { authentication ->
                    assertEquals(validTenant.apiKey, authentication.principal)
                    assertEquals(true, authentication.isAuthenticated)

                    val tenant = authentication.details as Tenant
                    assertEquals(validTenant.apiKey, tenant.apiKey)
                    assertEquals(validTenant.refreshAddress, tenant.refreshAddress)

                    assertThat(authentication.authorities, hasItem(SimpleGrantedAuthority(MANAGE_IDENTIFICATION_SESSION_AUTHORITY)))
                },
            )
        }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `should pass request without authentication object if header contains invalid api key`() {
        // Given
        val request = MockHttpServletRequest()
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)
        every { tenantProperties.findByApiKey(any()) } returns null
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer iAmNotValid")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(exactly = 0) { setAuthentication(any()) }
        verify { removeAuthentication() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `should pass request without authentication object if header is missing`() {
        // Given
        val request = MockHttpServletRequest()
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)
        every { tenantProperties.findByApiKey(any()) } returns null

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(exactly = 0) { setAuthentication(any()) }
        verify { removeAuthentication() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `should pass request without authentication object if header is malformed`() {
        // Given
        val request = MockHttpServletRequest()
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)
        request.addHeader(HttpHeaders.AUTHORIZATION, "BeaRinValid ${validTenant.apiKey}")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(exactly = 0) { setAuthentication(any()) }
        verify { removeAuthentication() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `should pass request without authentication object if header contains an empty string`() {
        // Given
        val request = MockHttpServletRequest()
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)
        every { tenantProperties.findByApiKey(any()) } returns null
        request.addHeader(HttpHeaders.AUTHORIZATION, "")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(exactly = 0) { setAuthentication(any()) }
        verify { removeAuthentication() }
        verify { filterChain.doFilter(request, response) }
    }
}
