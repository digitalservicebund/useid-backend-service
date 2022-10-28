package de.bund.digitalservice.useid.tracking

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(value = [OutputCaptureExtension::class, SpringExtension::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrackingWebRequestsTest : PostgresTestcontainerIntegrationTest() {

    @Autowired
    private lateinit var trackingWebRequests: TrackingWebRequests

    @Test
    fun `POST method should print 200 when given an url that returns 200 as status code`(output: CapturedOutput) {
        trackingWebRequests.POST("https://httpstat.us/200")
        MatcherAssert.assertThat(output.all, CoreMatchers.containsString("200"))
    }

    @Test
    fun `POST method should print 404 when given an url that returns 404 as status code`(output: CapturedOutput) {
        trackingWebRequests.POST("https://httpstat.us/404")
        MatcherAssert.assertThat(output.all, CoreMatchers.containsString("404"))
    }
}
