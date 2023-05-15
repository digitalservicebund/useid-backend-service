package de.bund.digitalservice.useid.tenant

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("test")
internal class TenantPropertiesTest {

    private val tenantProperties = TenantProperties()

    private val firstValidTenant = Tenant().apply {
        id = "valid-id-1"
        apiKey = "valid-api-key-1"
        refreshAddress = "valid-refresh-address-1"
        dataGroups = listOf("DG1", "DG2")
        allowedHosts = listOf("i.am.allowed.1", "i.am.also.allowed.1")
    }

    private val secondValidTenant = Tenant().apply {
        id = "valid-id-2"
        apiKey = "valid-api-key-2"
        refreshAddress = "valid-refresh-address-2"
        dataGroups = listOf("DG4", "DG17")
        allowedHosts = listOf("i.am.allowed.2", "i.am.also.allowed.2")
    }

    @BeforeAll
    fun beforeAll() {
        tenantProperties.tenants = listOf(firstValidTenant, secondValidTenant)
    }

    @Test
    fun findByApiKey() {
        var tenant = tenantProperties.findByApiKey(secondValidTenant.apiKey)
        assertThat(tenant!!.id).isEqualTo(secondValidTenant.id)

        tenant = tenantProperties.findByApiKey("invalid-api-key")
        assertThat(tenant).isEqualTo(null)
    }

    @Test
    fun findByAllowedHost() {
        var tenant = tenantProperties.findByAllowedHost(firstValidTenant.allowedHosts[1])
        assertThat(tenant!!.id).isEqualTo(firstValidTenant.id)

        tenant = tenantProperties.findByAllowedHost(secondValidTenant.allowedHosts[0])
        assertThat(tenant!!.id).isEqualTo(secondValidTenant.id)

        tenant = tenantProperties.findByAllowedHost("i.am.not.allowed")
        assertThat(tenant).isEqualTo(null)
    }

    @Test
    fun findByTenantId() {
        var tenant = tenantProperties.findByTenantId(firstValidTenant.id)
        assertThat(tenant!!.id).isEqualTo(firstValidTenant.id)

        tenant = tenantProperties.findByTenantId("iAmAnInvalidID")
        assertThat(tenant).isEqualTo(null)
    }
}
