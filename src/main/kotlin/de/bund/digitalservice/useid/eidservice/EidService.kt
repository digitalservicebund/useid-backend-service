package de.bund.digitalservice.useid.eidservice

import de.bund.bsi.eid230.AttributeRequestType
import de.bund.bsi.eid230.OperationsRequestorType
import de.bund.bsi.eid230.UseIDRequestType
import de.governikus.autent.sdk.eidservice.config.EidServiceConfiguration
import de.governikus.autent.sdk.eidservice.eidservices.EidService230
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

/*
    SDK DOCUMENTATION:
    Documentation about the SDK and the logic behind overriding the getWebserviceRequest function can be found in
    the Autent SDK Dokumentation Release 3.23.0.8 from Governikus KG -> 4.2.2 Implementierung des EidService

    E-ID-SERVER DOCUMENTATION:
    Documentation about the request attributes (AttributeRequestType) to be implemented can be found in
    Technical Guideline TR-03130 eID-Server Part 1: Functional Specification Version 2.4.0 -> Chapter 3.3 Data types (and following)
    https://www.bsi.bund.de/SharedDocs/Downloads/DE/BSI/Publikationen/TechnischeRichtlinien/TR03130/TR-03130_TR-eID-Server_Part1.pdf?__blob=publicationFile&v=3
 */
@Service
@ConditionalOnProperty("feature.eid-service-integration.enabled", havingValue = "true")
class EidService constructor(config: EidServiceConfiguration) : EidService230(config) {

    var dataGroups: List<String> = emptyList<String>()
    public override fun getWebserviceRequest(): UseIDRequestType {
        // val requestDataGroups = emptyList<String>() // TODO: Get dataGroups for this eService
        val request = UseIDRequestType()
        val selector = OperationsRequestorType()
        selector.givenNames = AttributeRequestType.REQUIRED

        // Data groups are only one function of the eID.
        // Further operators like age verification etc. can be added for further use cases.
        // if (requestDataGroups !== null && requestDataGroups.isNotEmpty()) {
        //     for (dataGroup in requestDataGroups) {
        //         when (dataGroup) {
        //             "DG1" -> selector.documentType = AttributeRequestType.REQUIRED
        //             "DG2" -> selector.issuingState = AttributeRequestType.REQUIRED
        //             "DG3" -> selector.dateOfExpiry = AttributeRequestType.REQUIRED
        //             "DG4" -> selector.givenNames = AttributeRequestType.REQUIRED
        //             "DG5" -> selector.familyNames = AttributeRequestType.REQUIRED
        //
        //             "DG7" -> selector.academicTitle = AttributeRequestType.REQUIRED
        //             "DG8" -> selector.dateOfBirth = AttributeRequestType.REQUIRED
        //             "DG9" -> selector.placeOfBirth = AttributeRequestType.REQUIRED
        //             "DG10" -> selector.nationality = AttributeRequestType.REQUIRED
        //
        //             "DG13" -> selector.birthName = AttributeRequestType.REQUIRED
        //
        //             "DG17" -> selector.placeOfResidence = AttributeRequestType.REQUIRED
        //
        //             "DG19" -> selector.residencePermitI = AttributeRequestType.REQUIRED
        //
        //             else -> {
        //                 throw IllegalStateException("Invalid data group for this eService")
        //             }
        //         }
        //     }
        // }
        request.useOperations = selector
        return request
    }
}
