package de.bund.digitalservice.useid

import mu.KLogger

inline fun <reified E : Throwable> KLogger.runIgnoring(crossinline messageProducer: (E) -> String, block: () -> Unit) =
    try {
        block()
    } catch (e: Throwable) {
        if (e is E) {
            error(messageProducer(e), e)
        } else {
            throw e
        }
    }

inline fun <reified E : Throwable> KLogger.runIgnoring(message: String, block: () -> Unit) =
    runIgnoring<E>({ message }, block)

inline fun <T> KLogger.runLogging(context: String, crossinline block: () -> T): T = try {
    info("$context: Begin")
    val result = block()
    info("$context: End")
    result
} catch (e: Throwable) {
    error("$context: Failed: ${e.message}", e)
    throw e
}
