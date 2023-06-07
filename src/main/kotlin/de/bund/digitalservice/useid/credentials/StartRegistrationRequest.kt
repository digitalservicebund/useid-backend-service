// PROTOTYPE FILE

package de.bund.digitalservice.useid.credentials

import java.util.UUID

data class StartRegistrationRequest(
    val widgetSessionId: UUID,
    val refreshAddress: String,
)
