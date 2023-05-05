package de.bund.digitalservice.useid.eidservice

import org.springframework.data.repository.CrudRepository
import java.util.Date

interface EidServiceRepository : CrudRepository<EidServiceHealthDataPoint, String> {
    fun save(result: Boolean) {
        save(EidServiceHealthDataPoint("${Date().toInstant()}", result, Date()))
    }
}
