package de.bund.digitalservice.useid.tenant

class Tenant {
    lateinit var id: String

    // api key information
    lateinit var apiKey: String
    lateinit var refreshAddress: String
    var dataGroups: List<String> = emptyList()

    // csp header information
    lateinit var defaultConfig: String
    lateinit var frameAncestors: String
    lateinit var allowedHost: String
}
