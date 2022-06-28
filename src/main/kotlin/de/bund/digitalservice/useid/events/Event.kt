package de.bund.digitalservice.useid.events

data class Event(val widgetSessionId: String, val encryptedRefreshAddress: String)
