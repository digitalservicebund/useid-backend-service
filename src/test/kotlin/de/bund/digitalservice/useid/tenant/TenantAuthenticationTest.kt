package de.bund.digitalservice.useid.tenant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("test")
internal class TenantAuthenticationTest {
    private val tenant = Tenant().apply {
        id = "valid-id"
        apiKey = "foobar"
        refreshAddress = "valid-refresh-address"
    }
    private val tenantAuthentication = TenantAuthentication(tenant)

    @Test
    fun getName() {
        assertEquals(null, tenantAuthentication.name)
    }

    @Test
    fun getDetails() {
        assertEquals(tenant.id, tenantAuthentication.details.id)
    }

    @Test
    fun getPrincipal() {
        assertEquals(tenant.apiKey, tenantAuthentication.principal)
    }

    @Test
    fun getAuthorities() {
        assertEquals(1, tenantAuthentication.authorities.size)
        assertEquals(MANAGE_IDENTIFICATION_SESSION_AUTHORITY, tenantAuthentication.authorities.first().authority)
    }

    @Test
    fun getCredentials() {
        assertEquals(tenant.apiKey, tenantAuthentication.credentials)
    }

    @Test
    fun setAuthenticated() {
        assertEquals(false, tenantAuthentication.isAuthenticated)
        tenantAuthentication.isAuthenticated = true
        assertEquals(true, tenantAuthentication.isAuthenticated)
    }
}
