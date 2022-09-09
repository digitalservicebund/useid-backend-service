package de.bund.digitalservice.useid.eidservice

import de.bund.bsi.eid230.UseIDRequestType
import de.governikus.autent.sdk.eidservice.config.EidServiceConfiguration
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Tag("test")
@Import(EidServiceProperties::class)
class EidServiceTest {

    @Autowired
    private lateinit var config: EidServiceConfiguration

    @Test
    fun `getWebserviceRequest returns a valid request`() {
        val eidService = EidService(config)
        val webserviceRequest = eidService.webserviceRequest
        assertThat(webserviceRequest, isA(UseIDRequestType::class.java))
    }
}
