package de.bund.digitalservice.useid.events

import java.util.UUID

class ConsumerNotFoundException(widgetSessionId: UUID) : Exception("No consumer found for widget session with id $widgetSessionId.")
