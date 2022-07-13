package de.bund.digitalservice.useid.events

class ConsumerNotFoundException(widgetSessionId: String) : Exception("No consumer found for widget session with id $widgetSessionId.")
