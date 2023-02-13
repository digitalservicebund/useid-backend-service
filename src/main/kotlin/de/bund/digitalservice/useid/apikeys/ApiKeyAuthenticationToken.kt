package de.bund.digitalservice.useid.apikeys

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

const val E_SERVICE_AUTHORITY = "E_SERVICE"

open class ApiKeyAuthenticationToken(
    private val apiKey: String,
    private val refreshAddress: String? = null,
    private val requestDataGroups: List<String> = emptyList(),
    private var authenticated: Boolean = false
) : Authentication {

    override fun getName(): String? {
        return null
    }

    override fun getAuthorities(): MutableCollection<GrantedAuthority> {
        return mutableSetOf(SimpleGrantedAuthority(E_SERVICE_AUTHORITY))
    }

    override fun getCredentials(): String {
        return apiKey
    }

    override fun getDetails(): ApiKeyDetails {
        return ApiKeyDetails(apiKey, refreshAddress, requestDataGroups)
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
