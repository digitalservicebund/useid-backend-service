package de.bund.digitalservice.useid.eidservice

import org.springframework.data.redis.core.RedisHash
import java.io.Serializable
import java.util.Date

@RedisHash("EidServiceHealth", timeToLive = 300)
data class EidServiceHealthDataPoint(val id: String, val up: Boolean, val timestamp: Date) : Serializable
