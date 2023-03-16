package de.bund.digitalservice.useid.tracking

import io.micrometer.core.instrument.Counter

data class SuccessRateCounters(
    val success: Counter,
    val failure: Counter,
)

inline fun <T> SuccessRateCounters.monitor(crossinline block: () -> T): T = try {
    val result = block()
    success.increment()
    result
} catch (e: Exception) {
    failure.increment()
    throw e
}
