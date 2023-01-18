package de.bund.digitalservice.useid.transactioninfo

import java.util.UUID

data class TransactionInfoDto(
    val useIdSessionId: UUID,
    val providerName: String,
    val providerURL: String,
    var additionalInfo: List<AdditionalInfo>
)
