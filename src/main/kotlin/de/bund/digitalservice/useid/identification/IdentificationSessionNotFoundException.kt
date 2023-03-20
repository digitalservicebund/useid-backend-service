package de.bund.digitalservice.useid.identification

import java.util.UUID

class IdentificationSessionNotFoundException(useIdSessionId: UUID) : Exception("No identification session found for useIdSessionId $useIdSessionId.")
