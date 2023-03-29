package de.bund.digitalservice.useid.tenant.tenants

open class Tenant {
    lateinit var id: String
    lateinit var apiKey: String
    lateinit var refreshAddress: String
    var dataGroups: List<String> = emptyList()
    var allowedHosts: List<String> = emptyList()
    var cspNonce: String = ""
    var cspHost: String = ""
}
