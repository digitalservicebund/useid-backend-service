package de.bund.digitalservice.useid.transactioninfo

data class TransactionInfo(
    val providerName: String,
    val providerURL: String,
    val additionalInfo: List<AdditionalInfo>
) {
    companion object {
        fun fromDto(transactionInfoDto: TransactionInfoDto): TransactionInfo {
            return TransactionInfo(
                transactionInfoDto.providerName,
                transactionInfoDto.providerURL,
                mapAdditionalInfo(transactionInfoDto)
            )
        }

        private fun mapAdditionalInfo(transactionInfoDto: TransactionInfoDto): List<AdditionalInfo> {
            return transactionInfoDto.additionalInfo.map {
                AdditionalInfo(it.key, it.value)
            }.toList()
        }
    }
}
