package de.bund.digitalservice.useid.eidservice

import org.springframework.data.redis.core.RedisHash
import java.io.Serializable
import java.util.Date

const val timewindowToConsiderForEidHealth: Long = 5 * 60

@RedisHash("EidServiceHealth", timeToLive = timewindowToConsiderForEidHealth)
data class EidServiceHealthDataPoint(val id: String, val up: Boolean, val timestamp: Date) : Serializable
