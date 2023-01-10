package de.bund.digitalservice.useid.transactioninfo

import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.UUID

internal const val TRANSACTION_INFO_SUFFIX = "transaction-info"

@RestController
@Tag(name = "Transaction Info", description = "Additional information regarding the identification which will be displayed in the eID-Client.")
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TransactionInfoController(
    private val transactionInfoService: TransactionInfoService
) {
    private val log = KotlinLogging.logger {}

    @PutMapping(
        path = ["/api/v1/identification/sessions/{useIDSessionId}/$TRANSACTION_INFO_SUFFIX"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createTransactionInfo(
        @PathVariable useIDSessionId: UUID,
        authentication: Authentication
    ): Mono<ResponseEntity<TransactionInfo>> {
        return transactionInfoService.createOrUpdate(useIDSessionId, "Spaßkasse", "https://www.sparkasse.de/", "Login bei der Spaßkasse")
            .map {
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(it)
            }
            .onErrorReturn(
                ResponseEntity.internalServerError().body(null)
            )
    }

    @GetMapping(
        path = ["/api/v1/identification/sessions/{useIDSessionId}/$TRANSACTION_INFO_SUFFIX"],
        produces = [MediaType.APPLICATION_XML_VALUE]
    )
    fun getTransactionInfo(@PathVariable useIDSessionId: UUID): Mono<ResponseEntity<TransactionInfo>> {
        return transactionInfoService.findByUseIDSessionId(useIDSessionId)
            .map {
                ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(it)
            }
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null))
            .doOnError { exception ->
                log.error("Failed to get tc token for identification session. useidSessionId=$useIDSessionId", exception)
            }
            .onErrorReturn(
                ResponseEntity.internalServerError().body(null)
            )
    }
}
