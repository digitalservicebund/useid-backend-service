package de.bund.digitalservice.useid

enum class EventStatus(val eventName: String) {
    READY("ready"),
    IN_PROGRESS("ping"),
    FINISHED("close")
}
