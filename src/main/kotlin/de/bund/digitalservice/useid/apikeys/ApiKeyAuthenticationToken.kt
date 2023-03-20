package de.bund.digitalservice.useid.apikeys

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

const val MANAGE_IDENTIFICATION_SESSION_AUTHORITY = "MANAGE_IDENTIFICATION_SESSION"

open class ApiKeyAuthenticationToken(
    private val apiKey: String,
    private val refreshAddress: String? = null,
    private val requestDataGroups: List<String> = emptyList(),
    private var authenticated: Boolean = false,
    private val tenantId: String? = null,
) : Authentication {

    override fun getName(): String? {
        return null
    }

    override fun getAuthorities(): MutableCollection<GrantedAuthority> {
        return mutableSetOf(SimpleGrantedAuthority(MANAGE_IDENTIFICATION_SESSION_AUTHORITY))
    }

    override fun getCredentials(): String {
        return apiKey
    }

    override fun getDetails(): ApiKeyDetails {
        return ApiKeyDetails(apiKey, refreshAddress, requestDataGroups, tenantId)
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
