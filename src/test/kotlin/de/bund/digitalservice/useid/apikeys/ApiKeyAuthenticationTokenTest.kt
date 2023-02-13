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
        assertEquals(null, apiKeyAuthenticationToken.name)
    }

    @Test
    fun getAuthorities() {
        assertEquals(1, apiKeyAuthenticationToken.authorities.size)
        assertEquals(E_SERVICE_AUTHORITY, apiKeyAuthenticationToken.authorities.first().authority)
    }

    @Test
    fun getCredentials() {
        assertEquals(apiKey, apiKeyAuthenticationToken.credentials)
    }

    @Test
    fun setAuthenticated() {
        assertEquals(false, apiKeyAuthenticationToken.isAuthenticated)
        apiKeyAuthenticationToken.isAuthenticated = true
        assertEquals(true, apiKeyAuthenticationToken.isAuthenticated)
    }
}
