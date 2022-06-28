package de.bund.digitalservice.useid.events

data class Event(val consumerId: String, val encryptedRefreshAddress: String, val widgetSessionId: String)
