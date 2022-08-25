package de.bund.digitalservice.useid.eidservice

import de.bund.bsi.eid230.AttributeRequestType
import de.bund.bsi.eid230.OperationsRequestorType
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
class EidService constructor(config: EidServiceConfiguration) : EidService230(config) {
    // TODO: The data attributes (for example: name, age) request should be configurable for each eService
    public override fun getWebserviceRequest(): UseIDRequestType {
        val request = UseIDRequestType()

        val selector = OperationsRequestorType()
        selector.givenNames = AttributeRequestType.REQUIRED
        selector.placeOfResidence = AttributeRequestType.ALLOWED
        selector.birthName = AttributeRequestType.ALLOWED
        request.useOperations = selector

        return request
    }
}
