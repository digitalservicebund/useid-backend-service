package de.bund.digitalservice.useid.tenant

import org.assertj.core.api.Assertions.assertThat
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
        assertThat(tenantAuthentication.name).isEqualTo(null)
    }

    @Test
    fun getDetails() {
        assertThat(tenantAuthentication.details.id).isEqualTo(tenant.id)
    }

    @Test
    fun getPrincipal() {
        assertThat(tenantAuthentication.principal).isEqualTo(tenant.apiKey)
    }

    @Test
    fun getAuthorities() {
        assertThat(tenantAuthentication.authorities.size).isEqualTo(1)
        assertThat(tenantAuthentication.authorities.first().authority).isEqualTo(MANAGE_IDENTIFICATION_SESSION_AUTHORITY)
    }

    @Test
    fun getCredentials() {
        assertThat(tenantAuthentication.credentials).isEqualTo(tenant.apiKey)
    }

    @Test
    fun setAuthenticated() {
        assertThat(tenantAuthentication.isAuthenticated).isEqualTo(false)
        tenantAuthentication.isAuthenticated = true
        assertThat(tenantAuthentication.isAuthenticated).isEqualTo(true)
    }
}
