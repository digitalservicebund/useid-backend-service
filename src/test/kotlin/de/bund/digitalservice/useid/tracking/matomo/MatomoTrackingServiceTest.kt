package de.bund.digitalservice.useid.tracking.matomo

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(value = [OutputCaptureExtension::class, SpringExtension::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MatomoTrackingServiceTest : PostgresTestcontainerIntegrationTest() {

    @Autowired
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Test
    fun `matomo tracking service should trigger web request and log event category, action and name and code 200`(output: CapturedOutput) {
        val log1 = "logged"
        val log2 = "printed"
        val log3 = "output"

        val matomoEvent = MatomoEvent(this, log1, log2, log3)
        applicationEventPublisher.publishEvent(matomoEvent)

        MatcherAssert.assertThat(output.all, CoreMatchers.containsString(log1))
        MatcherAssert.assertThat(output.all, CoreMatchers.containsString(log2))
        MatcherAssert.assertThat(output.all, CoreMatchers.containsString(log3))
        MatcherAssert.assertThat(output.all, CoreMatchers.containsString("200"))
    }
}
