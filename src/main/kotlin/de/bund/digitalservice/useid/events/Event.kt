package de.bund.digitalservice.useid.events

open class Event(val success: Boolean)

data class SuccessEvent(val refreshAddress: String) : Event(success = true)
data class ErrorEvent(val message: String) : Event(success = false)
