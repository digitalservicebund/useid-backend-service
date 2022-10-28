package de.bund.digitalservice.useid.tracking.matomo

import de.bund.digitalservice.useid.tracking.TrackingProperties
import de.bund.digitalservice.useid.tracking.WebRequests
import mu.KotlinLogging
import org.springframework.context.ApplicationListener
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service for sending tracking events to the matomo server.
 * The service implements an ApplicationListener for a custom MatomoEvent object/event.
 *
 * Documentation about the tracking api:
 * https://developer.matomo.org/api-reference/tracking-api
 *
 * Documentation about matomo events
 * https://matomo.org/guide/reports/event-tracking/
 */
// @Profile("!local")
@Service
class MatomoTrackingService(trackingProperties: TrackingProperties, private val webRequests: WebRequests) : ApplicationListener<MatomoEvent> {

    private val log = KotlinLogging.logger {}
    private val siteId = trackingProperties.matomo.siteId
    private val domain = trackingProperties.matomo.domain

    private fun constructEventURL(e: MatomoEvent): String {
        val url = "$domain?idsite=$siteId&rec=1&ca=1&e_c=${e.category}&e_a=${e.action}&e_n=${e.name}"
        log.debug { url }
        return url
    }

    override fun onApplicationEvent(e: MatomoEvent) {
        Mono.fromCallable {
            constructEventURL(e)
        }.flatMap {
            webRequests.POST(it)
        }.map {
            if (it == HttpStatus.OK) {
                log.info("$it, successfully tracked: $url")
            } else {
                log.error("$it, tracking failed for: $url")
            }
        }
    }
}
