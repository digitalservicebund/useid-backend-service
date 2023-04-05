package de.bund.digitalservice.useid.credentials

import java.util.UUID

class InvalidCredentialException(credentialId: UUID) : Exception("Invalid credential. credentialId=$credentialId.")
