package de.bund.digitalservice.useid.model

data class CreateIdentitySessionResponse(
    val tcTokenUrl: String,
    val sessionId: String
)
