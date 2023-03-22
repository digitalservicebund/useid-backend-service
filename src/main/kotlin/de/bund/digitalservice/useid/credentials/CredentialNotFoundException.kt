package de.bund.digitalservice.useid.credentials

import java.util.UUID

class CredentialNotFoundException(credentialId: UUID) : Exception("No credential found. credentialId=$credentialId.")
