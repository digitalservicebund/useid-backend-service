package de.bund.digitalservice.useid.eidservice

import de.bund.bsi.eid230.AttributeRequestType
import org.hamcrest.CoreMatchers.`is`
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
    private lateinit var eidService: EidService

    @Test
    fun `getWebserviceRequest returns a valid request`() {
        val webserviceRequest = eidService.webserviceRequest

        assertThat(webserviceRequest.useOperations.givenNames, `is`(AttributeRequestType.REQUIRED))
        assertThat(webserviceRequest.useOperations.placeOfResidence, `is`(AttributeRequestType.ALLOWED))
        assertThat(webserviceRequest.useOperations.birthName, `is`(AttributeRequestType.ALLOWED))
    }
}
