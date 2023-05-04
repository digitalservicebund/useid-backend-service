package de.bund.digitalservice.useid.eidservice

import org.springframework.data.repository.CrudRepository

interface EidServiceRepository : CrudRepository<EidServiceHealthDataPoint, String>
