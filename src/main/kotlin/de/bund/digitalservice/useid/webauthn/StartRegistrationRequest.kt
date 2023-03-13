package de.bund.digitalservice.useid.webauthn

import java.util.UUID

data class StartRegistrationRequest(
    val widgetSessionId: UUID,
    val refreshAddress: String,
)
