package de.bund.digitalservice.useid.eventstreams

enum class EventType(val eventName: String) {
    SUCCESS("success"),
    ERROR("error"),
    AUTHENTICATE("authenticate"),
}
