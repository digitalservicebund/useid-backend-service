package de.bund.digitalservice.useid.ping

enum class EventStatusConfig(val eventName: String) {
    READY("ready"),
    IN_PROGRESS("ping"),
    FINISHED("close")
}
