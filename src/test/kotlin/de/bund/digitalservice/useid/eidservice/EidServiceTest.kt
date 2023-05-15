package de.bund.digitalservice.useid.eidservice

import de.bund.bsi.eid230.AttributeRequestType
import de.bund.bsi.eid230.UseIDRequestType
import de.governikus.autent.sdk.eidservice.config.EidServiceConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [EidServiceConfig::class])
@Tag("test")
@EnableConfigurationProperties(EidServiceProperties::class)
class EidServiceTest {

    @Autowired
    private lateinit var config: EidServiceConfiguration

    @Test
    fun `getWebserviceRequest returns a valid request with no operations set when passed no dataGroups`() {
        val eidService = EidService(config)
        val webserviceRequest = eidService.webserviceRequest
        assertThat(webserviceRequest)
            .isInstanceOf(UseIDRequestType::class.java)

        val operations = webserviceRequest.useOperations
        assertThat(operations.documentType).isNull()
        assertThat(operations.issuingState).isNull()
        assertThat(operations.dateOfExpiry).isNull()
        assertThat(operations.givenNames).isNull()
        assertThat(operations.familyNames).isNull()
        assertThat(operations.academicTitle).isNull()
        assertThat(operations.dateOfBirth).isNull()
        assertThat(operations.placeOfBirth).isNull()
        assertThat(operations.nationality).isNull()
        assertThat(operations.birthName).isNull()
        assertThat(operations.placeOfResidence).isNull()
        assertThat(operations.residencePermitI).isNull()
    }

    @Test
    fun `getWebserviceRequest includes specified data groups as part of operations in request`() {
        val eidService = EidService(config, listOf("DG1", "DG2", "DG3", "DG4", "DG5", "DG7", "DG8", "DG9", "DG10", "DG13", "DG17", "DG19"))
        val operations = eidService.webserviceRequest.useOperations
        val required = AttributeRequestType.REQUIRED
        assertThat(required).isEqualTo(operations.documentType)
        assertThat(required).isEqualTo(operations.issuingState)
        assertThat(required).isEqualTo(operations.dateOfExpiry)
        assertThat(required).isEqualTo(operations.givenNames)
        assertThat(required).isEqualTo(operations.familyNames)
        assertThat(required).isEqualTo(operations.academicTitle)
        assertThat(required).isEqualTo(operations.dateOfBirth)
        assertThat(required).isEqualTo(operations.placeOfBirth)
        assertThat(required).isEqualTo(operations.nationality)
        assertThat(required).isEqualTo(operations.birthName)
        assertThat(required).isEqualTo(operations.placeOfResidence)
        assertThat(required).isEqualTo(operations.residencePermitI)
    }

    @Test
    fun `getWebserviceRequest throws IllegalStateException when passed invalid list items as data groups`() {
        val eidService = EidService(config, listOf("iAmInvalid", "meToo"))
        assertThatThrownBy {
            eidService.webserviceRequest.useOperations
        }.isInstanceOf(IllegalStateException::class.java)
    }
}
