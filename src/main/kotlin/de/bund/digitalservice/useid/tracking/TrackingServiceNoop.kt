package de.bund.digitalservice.useid.tracking

import mu.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("local")
class TrackingServiceNoop : TrackingServiceInterface {
    private val log = KotlinLogging.logger {}
    override fun sendMatomoEvent(category: String, action: String, name: String) {
        log.debug("triggered sendMatomoEvent: $category, $action, $name")
    }
}
