package de.bund.digitalservice.useid.apikeys

import de.bund.digitalservice.useid.config.Tenant
import de.bund.digitalservice.useid.config.TenantProperties
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.core.authority.SimpleGrantedAuthority

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class ApiKeyAuthenticationFilterTest {

    @Autowired
    private lateinit var tenantProperties: TenantProperties

    private lateinit var manager: ApiKeyAuthenticationManager
    private lateinit var filter: ApiKeyAuthenticationFilter

    private val validTenant = Tenant().apply {
        apiKey = "valid-api-key"
        refreshAddress = "some-refresh-address"
    }

    @BeforeAll
    fun setup() {
        println(tenantProperties.tenants[0].apiKey)

        manager = ApiKeyAuthenticationManager(tenantProperties)
        filter = ApiKeyAuthenticationFilter(manager)
    }

    @BeforeEach
    fun beforeEach() {
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
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer ${validTenant.apiKey}")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify {
            setAuthentication(
                withArg { authentication ->
                    assertEquals(validTenant.apiKey, authentication.principal)
                    assertEquals(true, authentication.isAuthenticated)

                    val apiKeyDetails = authentication.details as ApiKeyDetails
                    assertEquals(validTenant.apiKey, apiKeyDetails.keyValue)
                    assertEquals(validTenant.refreshAddress, apiKeyDetails.refreshAddress)

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
        request.addHeader(HttpHeaders.AUTHORIZATION, "")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(exactly = 0) { setAuthentication(any()) }
        verify { removeAuthentication() }
        verify { filterChain.doFilter(request, response) }
    }
}
