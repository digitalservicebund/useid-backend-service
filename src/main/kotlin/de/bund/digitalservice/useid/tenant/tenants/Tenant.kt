package de.bund.digitalservice.useid.tenant.tenants

open class Tenant {
    lateinit var id: String
    lateinit var apiKey: String
    lateinit var refreshAddress: String
    var dataGroups: List<String> = emptyList()
    var allowedHosts: List<String> = emptyList()
    lateinit var cspNonce: String
    lateinit var cspHost: String
}
