package de.bund.digitalservice.useid.utils

import java.util.UUID

class IdGenerator {
    companion object {
        fun generateUUID() = UUID.randomUUID().toString()
    }
}
