package de.bund.digitalservice.useid.tracking

interface TrackingServiceInterface {
    fun sendMatomoEvent(category: String, action: String, name: String)
}
