package de.bund.digitalservice.useid.identification

import org.springframework.stereotype.Service

@Service
class IdentificationSessionHandler(private val mockDatasource: MockDatasource) {

    fun save(sessionId: String, tcTokenUrl: String, refreshAddress: String, requestAttributes: List<String>) {
        mockDatasource.save(sessionId, tcTokenUrl, refreshAddress, requestAttributes)
    }

    fun hasValidSessionId(sessionId: String): Boolean {
        if (!mockDatasource.hasValidSessionId(sessionId)) {
            throw NoSuchElementException("Error: sessionId is not found")
        }
        return mockDatasource.hasValidSessionId(sessionId)
    }
}
