package de.bund.digitalservice.useid.events

import java.util.UUID

data class AuthenticateEvent(val credentialId: UUID, val credentialGetJson: String)
