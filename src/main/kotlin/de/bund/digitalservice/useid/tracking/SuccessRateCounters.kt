package de.bund.digitalservice.useid.tracking

import io.micrometer.core.instrument.Counter

data class SuccessRateCounters(
    val success: Counter,
    val failure: Counter,
)
