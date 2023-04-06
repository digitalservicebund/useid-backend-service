package de.bund.digitalservice.useid.eventstreams

import java.util.UUID

data class AuthenticateEvent(val credentialId: UUID, val credentialGetJson: String)
