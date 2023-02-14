package de.bund.digitalservice.useid.apikeys

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletResponse

@Tag("test")
internal class ApiKeyAuthenticationFilterTest {

    private val apiProperties: ApiProperties = mockk()

    private val manager = ApiKeyAuthenticationManager(apiProperties)
    private val filter = ApiKeyAuthenticationFilter(manager)
    private val validApiKey = ApiProperties.ApiKey().apply {
        keyValue = "valid-api-key"
        refreshAddress = "some-refresh-address"
    }

    @BeforeEach
    fun beforeEach() {
        mockkStatic(SecurityContextHolder::class)
        every { SecurityContextHolder.clearContext() } returns Unit
        every { apiProperties.apiKeys } returns listOf(validApiKey)
    }

    @AfterEach
    fun afterEach() {
        unmockkStatic(SecurityContextHolder::class)
    }

    @Test
    fun `should pass request with authentication object if header contains valid api key`() {
        // Given
        val request = MockHttpServletRequest()
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)
        val context: SecurityContext = mockk(relaxed = true)
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer ${validApiKey.keyValue}")
        every { SecurityContextHolder.getContext() } returns context

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify {
            context.authentication = withArg { authentication ->
                assertEquals(validApiKey.keyValue, authentication.principal)
                assertEquals(true, authentication.isAuthenticated)

                val apiKeyDetails = authentication.details as ApiKeyDetails
                assertEquals(validApiKey.keyValue, apiKeyDetails.keyValue)
                assertEquals(validApiKey.refreshAddress, apiKeyDetails.refreshAddress)

                assertThat(authentication.authorities, hasItem(SimpleGrantedAuthority(MANAGE_IDENTIFICATION_SESSION_AUTHORITY)))
            }
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
        verify(exactly = 0) { SecurityContextHolder.getContext() }
        verify { SecurityContextHolder.clearContext() }
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
        verify(exactly = 0) { SecurityContextHolder.getContext() }
        verify { SecurityContextHolder.clearContext() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `should pass request without authentication object if header is malformed`() {
        // Given
        val request = MockHttpServletRequest()
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)
        request.addHeader(HttpHeaders.AUTHORIZATION, "BeaRinValid ${validApiKey.keyValue}")

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(exactly = 0) { SecurityContextHolder.getContext() }
        verify { SecurityContextHolder.clearContext() }
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
        verify(exactly = 0) { SecurityContextHolder.getContext() }
        verify { SecurityContextHolder.clearContext() }
        verify { filterChain.doFilter(request, response) }
    }
}
