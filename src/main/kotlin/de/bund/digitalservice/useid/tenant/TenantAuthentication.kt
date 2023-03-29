package de.bund.digitalservice.useid.tenant

import de.bund.digitalservice.useid.tenant.tenants.Tenant
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

const val MANAGE_IDENTIFICATION_SESSION_AUTHORITY = "MANAGE_IDENTIFICATION_SESSION"

open class TenantAuthentication(
    private val tenant: Tenant,
    private var authenticated: Boolean = false,
) : Authentication {

    override fun getName(): String? {
        return null
    }

    override fun getAuthorities(): MutableCollection<GrantedAuthority> {
        return mutableSetOf(SimpleGrantedAuthority(MANAGE_IDENTIFICATION_SESSION_AUTHORITY))
    }

    override fun getCredentials(): String {
        return tenant.apiKey
    }

    override fun getDetails(): Tenant {
        return tenant
    }

    override fun getPrincipal(): String {
        return tenant.apiKey
    }

    override fun isAuthenticated(): Boolean {
        return authenticated
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        authenticated = isAuthenticated
    }
}
