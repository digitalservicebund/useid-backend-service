package de.bund.digitalservice.useid.eidservice

import org.springframework.data.redis.core.RedisHash
import java.io.Serializable
import java.util.Date

const val timewindowToConsiderForEidAvailibiltyInMinutes: Long = 5 * 60

@RedisHash("EidServiceAvailability", timeToLive = timewindowToConsiderForEidAvailibiltyInMinutes)
data class EidAvailabilityCheck(val id: String, val up: Boolean, val timestamp: Date) : Serializable
