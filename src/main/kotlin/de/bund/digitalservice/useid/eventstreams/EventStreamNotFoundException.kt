package de.bund.digitalservice.useid.eventstreams

import java.util.UUID

class EventStreamNotFoundException(eventStreamId: UUID) : Exception("No consumer found for this event stream. eventStreamId=$eventStreamId.")
