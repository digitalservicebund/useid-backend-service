package de.bund.digitalservice.useid.apikeys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("test")
internal class ApiKeyAuthenticationTokenTest {
    private val apiKey = "foobar"
    private val apiKeyAuthenticationToken = ApiKeyAuthenticationToken(apiKey)

    @Test
    fun getName() {
        assertEquals(apiKeyAuthenticationToken.name, null)
    }

    @Test
    fun getAuthorities() {
        assertEquals(apiKeyAuthenticationToken.authorities.size, 0)
    }

    @Test
    fun getCredentials() {
        assertEquals(apiKeyAuthenticationToken.credentials, apiKey)
    }

    @Test
    fun setAuthenticated() {
        assertEquals(apiKeyAuthenticationToken.isAuthenticated, false)
        apiKeyAuthenticationToken.isAuthenticated = true
        assertEquals(apiKeyAuthenticationToken.isAuthenticated, true)
    }
}
