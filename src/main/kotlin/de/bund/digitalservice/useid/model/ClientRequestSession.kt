package de.bund.digitalservice.useid.model

data class ClientRequestSession(
    val refreshAddress: String,
    val requestAttributes: List<String>
)
