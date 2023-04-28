package de.bund.digitalservice.useid.transactioninfo

import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.documentation.EIDClientTag
import de.bund.digitalservice.useid.documentation.EServiceTag
import io.swagger.v3.oas.annotations.security.SecurityRequirement
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

internal const val TRANSACTION_INFO_SUBPATH = "transaction-infos"

@RestController
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TransactionInfoController(
    private val transactionInfoService: TransactionInfoService,
) {
    @PostMapping(
        path = ["${ApplicationProperties.apiVersionPrefix}/identifications/{useIdSessionId}/$TRANSACTION_INFO_SUBPATH"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @EServiceTag
    @SecurityRequirement(name = "apiKey")
    fun createTransactionInfo(
        @PathVariable useIdSessionId: UUID,
        @RequestBody transactionInfo: TransactionInfo,
    ): ResponseEntity<TransactionInfo> {
        val transactionInfoDto = transactionInfoService.create(useIdSessionId, transactionInfo)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TransactionInfo.fromDto(transactionInfoDto))
    }

    @GetMapping(
        path = ["${ApplicationProperties.apiVersionPrefix}/$TRANSACTION_INFO_SUBPATH/{useIdSessionId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @EIDClientTag
    fun getTransactionInfo(@PathVariable useIdSessionId: UUID): ResponseEntity<TransactionInfo> {
        val transactionInfoDto = transactionInfoService.findByUseIdSessionId(useIdSessionId) ?: return ResponseEntity.notFound().build()

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TransactionInfo.fromDto(transactionInfoDto))
    }
}
