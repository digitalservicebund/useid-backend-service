package de.bund.digitalservice.useid.identification

data class CreateIdentitySessionRequest(
    val refreshAddress: String,
    val requestAttributes: List<String>
)
