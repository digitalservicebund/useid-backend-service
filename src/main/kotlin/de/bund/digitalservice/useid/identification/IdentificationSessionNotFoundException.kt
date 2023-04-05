package de.bund.digitalservice.useid.identification

import java.util.UUID

class IdentificationSessionNotFoundException(useIdSessionId: UUID? = null) :
    Exception("""No identification session found. ${if (useIdSessionId != null) "useIdSessionId=$useIdSessionId" else ""}""")
