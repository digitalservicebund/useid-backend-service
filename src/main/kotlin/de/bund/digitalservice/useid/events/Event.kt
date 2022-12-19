package de.bund.digitalservice.useid.events

import java.util.UUID

data class Event(val widgetSessionId: UUID, val refreshAddress: String)
