package de.bund.digitalservice.useid.apikeys

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.Assertions.assertEquals
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

    @Test
    fun `no auth`() {
        // Given
        val request = MockHttpServletRequest()
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)
        mockkStatic(SecurityContextHolder::class)

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify(exactly = 0) { SecurityContextHolder.getContext() }
        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `test`() {
        // Given
        val request = MockHttpServletRequest()
        val apiKeyValue = "valid-api-key"
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $apiKeyValue")
        val response: HttpServletResponse = mockk(relaxed = true)
        val filterChain: FilterChain = mockk(relaxed = true)
        mockkStatic(SecurityContextHolder::class)
        val validApiKey = ApiProperties.ApiKey()
        validApiKey.keyValue = apiKeyValue
        val refreshAddress = "some-refresh-address"
        validApiKey.refreshAddress = refreshAddress
        every { apiProperties.apiKeys } returns listOf(validApiKey)
        val context: SecurityContext = mockk(relaxed = true)
        every { SecurityContextHolder.getContext() } returns context
        every { context.authentication }

        // When
        filter.doFilter(request, response, filterChain)

        // Then
        verify {
            context.authentication = withArg { authentication ->
                assertEquals(apiKeyValue, authentication.principal)
                assertEquals(true, authentication.isAuthenticated)

                val apiKeyDetails = authentication.details as ApiKeyDetails
                assertEquals(apiKeyValue, apiKeyDetails.keyValue)
                assertEquals(refreshAddress, apiKeyDetails.refreshAddress)

                assertThat(authentication.authorities, hasItem(SimpleGrantedAuthority(E_SERVICE_AUTHORITY)))
            }
        }
        verify { filterChain.doFilter(request, response) }
    }
}
