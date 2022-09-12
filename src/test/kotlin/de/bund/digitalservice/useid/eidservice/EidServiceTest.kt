package de.bund.digitalservice.useid.eidservice

import de.bund.bsi.eid230.AttributeRequestType
import de.bund.bsi.eid230.UseIDRequestType
import de.governikus.autent.sdk.eidservice.config.EidServiceConfiguration
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
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

    @Test
    fun `getWebserviceRequest includes specified data groups as part of operations in request`() {
        val eidService = EidService(config, listOf("DG1", "DG2", "DG3", "DG4", "DG5", "DG7", "DG8", "DG9", "DG10", "DG13", "DG17", "DG19"))
        val operations = eidService.webserviceRequest.useOperations
        val required = AttributeRequestType.REQUIRED
        assertEquals(operations.documentType, required)
        assertEquals(operations.issuingState, required)
        assertEquals(operations.dateOfExpiry, required)
        assertEquals(operations.givenNames, required)
        assertEquals(operations.familyNames, required)
        assertEquals(operations.academicTitle, required)
        assertEquals(operations.dateOfBirth, required)
        assertEquals(operations.placeOfBirth, required)
        assertEquals(operations.nationality, required)
        assertEquals(operations.birthName, required)
        assertEquals(operations.placeOfResidence, required)
        assertEquals(operations.residencePermitI, required)
    }

    @Test
    fun `getWebserviceRequest throws IllegalStateException when passed invalid list items as data groups`() {
        val eidService = EidService(config, listOf("iAmInvalid", "meToo"))
        Assertions.assertThrows(IllegalStateException::class.java) {
            eidService.webserviceRequest.useOperations
        }
    }

}
