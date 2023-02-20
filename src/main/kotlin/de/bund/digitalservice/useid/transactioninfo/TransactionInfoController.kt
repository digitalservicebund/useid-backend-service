package de.bund.digitalservice.useid.transactioninfo

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

internal const val TRANSACTION_INFO_SUFFIX = "transaction-info"

@RestController
@Tag(name = "Transaction Info", description = "Additional information regarding the identification which will be displayed in the eID-Client.")
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TransactionInfoController(
    private val transactionInfoService: TransactionInfoService
) {
    @PostMapping(
        path = ["/api/v1/identification/sessions/{useIdSessionId}/$TRANSACTION_INFO_SUFFIX"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun createTransactionInfo(
        @PathVariable useIdSessionId: UUID,
        @RequestBody transactionInfo: TransactionInfo
    ): ResponseEntity<TransactionInfo> {
        val transactionInfoDto = transactionInfoService.create(useIdSessionId, transactionInfo)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TransactionInfo.fromDto(transactionInfoDto))
    }

    @GetMapping(
        path = ["/api/v1/identification/sessions/{useIdSessionId}/$TRANSACTION_INFO_SUFFIX"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getTransactionInfo(@PathVariable useIdSessionId: UUID): ResponseEntity<TransactionInfo> {
        val transactionInfoDto = transactionInfoService.findByUseIdSessionId(useIdSessionId) ?: return ResponseEntity.notFound().build()

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TransactionInfo.fromDto(transactionInfoDto))
    }
}
