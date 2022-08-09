package de.bund.digitalservice.useid.apikeys

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class ApiKeyAuthenticationToken(private val apiKey: String, private val refreshAddress: String? = null, private var authenticated: Boolean = false) : Authentication {

    override fun getName(): String? {
        return null
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableSetOf() // No specific authorities granted
    }

    override fun getCredentials(): String {
        return apiKey
    }

    override fun getDetails(): ApiKeyDetails {
        return ApiKeyDetails(apiKey, refreshAddress)
    }

    override fun getPrincipal(): String {
        return apiKey
    }

    override fun isAuthenticated(): Boolean {
        return authenticated
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        authenticated = isAuthenticated
    }
}
