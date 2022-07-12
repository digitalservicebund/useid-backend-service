package de.bund.digitalservice.useid.model

data class CreateIdentitySessionRequest(
    val refreshAddress: String,
    val requestAttributes: List<String>
)
