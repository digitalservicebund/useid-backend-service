package de.bund.digitalservice.useid.eidservice

import org.springframework.data.repository.CrudRepository

interface EidAvailabilityRepository : CrudRepository<EidAvailabilityCheck, String>
