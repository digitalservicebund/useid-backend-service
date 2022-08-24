package de.bund.digitalservice.useid.eidservice

import de.bund.bsi.eid230.AgeVerificationRequestType
import de.bund.bsi.eid230.AttributeRequestType
import de.bund.bsi.eid230.OperationsRequestorType
import de.bund.bsi.eid230.PlaceVerificationRequestType
import de.bund.bsi.eid230.UseIDRequestType
import de.governikus.autent.sdk.eidservice.config.EidServiceConfiguration
import de.governikus.autent.sdk.eidservice.eidservices.EidService230
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

/*
    The details of data attributes can be found in the TR03130 — BSI eID server technical guideline:
    https://www.bsi.bund.de/SharedDocs/Downloads/DE/BSI/Publikationen/TechnischeRichtlinien/TR03130/TR-03130_TR-eID-Server_Part1.pdf?__blob=publicationFile&v=3
    Page: 19 and following
 */
@Service
@ConditionalOnProperty("feature.eid-service-integration.enabled", havingValue = "true")
class EidService private constructor(config: EidServiceConfiguration) : EidService230(config) {
    // TODO: The data attributes (for example: name, age) request should be configurable for each eService
    override fun getWebserviceRequest(): UseIDRequestType {
        val request = UseIDRequestType()
        val ageVerification = AgeVerificationRequestType()
        ageVerification.age = 21
        request.ageVerificationRequest = ageVerification

        val placeVerification = PlaceVerificationRequestType()
        placeVerification.communityID = "02760503150000"
        request.placeVerificationRequest = placeVerification

        val selector = OperationsRequestorType()
        selector.givenNames = AttributeRequestType.ALLOWED
        selector.ageVerification = AttributeRequestType.ALLOWED
        selector.placeVerification = AttributeRequestType.REQUIRED
        selector.placeOfResidence = AttributeRequestType.REQUIRED
        selector.birthName = AttributeRequestType.ALLOWED
        selector.residencePermitI = AttributeRequestType.ALLOWED
        request.useOperations = selector

        return request
    }
}
