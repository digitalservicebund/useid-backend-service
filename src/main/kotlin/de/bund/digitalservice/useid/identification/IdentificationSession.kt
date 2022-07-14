package de.bund.digitalservice.useid.identification

data class IdentificationSession(
    val refreshAddress: String,
    val requestAttributes: List<String>,
    val tcTokenUrl: String
)
