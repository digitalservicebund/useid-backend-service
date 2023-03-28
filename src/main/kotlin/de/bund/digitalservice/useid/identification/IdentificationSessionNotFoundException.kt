package de.bund.digitalservice.useid.identification

import java.util.UUID

class IdentificationSessionNotFoundException(useIdSessionId: UUID?) : Exception("No identification session found. useIdSessionId=$useIdSessionId") // TODO do not include id if null
