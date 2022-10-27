package de.bund.digitalservice.useid.tracking.matomo

import de.bund.digitalservice.useid.config.ApplicationProperties
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Import

@Tag("test")
@Import(value = [ApplicationProperties::class])
class MatomoTrackingServiceTest(private val applicationEventPublisher: ApplicationEventPublisher) {
    @Test
    fun `get identity data endpoint - eidService getEidInformation method should log error message`(output: CapturedOutput) {
        val log1 = "This should be logged"
        val log2 = "That shall become printed"
        val log3 = "And another output"

        val matomoEvent = MatomoEvent(this, log1, log2, log3)
        applicationEventPublisher.publishEvent(matomoEvent)

        // maybe not enough time in between ??

        MatcherAssert.assertThat(output.all, CoreMatchers.containsString(log1))
        MatcherAssert.assertThat(output.all, CoreMatchers.containsString(log2))
        MatcherAssert.assertThat(output.all, CoreMatchers.containsString(log3))
    }
}
