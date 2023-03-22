package de.bund.digitalservice.useid.events

enum class EventType(val eventName: String) {
    SUCCESS("success"),
    ERROR("error"),
    AUTHENTICATE("authenticate"),
}
