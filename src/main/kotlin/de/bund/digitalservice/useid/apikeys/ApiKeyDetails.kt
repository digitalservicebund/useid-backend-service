package de.bund.digitalservice.useid.apikeys

class ApiKeyDetails(val keyValue: String, val refreshAddress: String?, val requestDataGroups: List<String>) {
    fun getTentant(refreshAddress: String): String {
        if (refreshAddress == "...") {
            return "demo"
        }
        return ""
    }
}
