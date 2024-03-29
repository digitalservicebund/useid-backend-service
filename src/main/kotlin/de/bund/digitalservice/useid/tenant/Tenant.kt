package de.bund.digitalservice.useid.tenant

class Tenant {
    lateinit var id: String
    lateinit var apiKey: String
    lateinit var refreshAddress: String
    lateinit var dataGroups: List<String>
    lateinit var allowedHosts: List<String>
    lateinit var cspHost: String
}
