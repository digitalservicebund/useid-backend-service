package de.bund.digitalservice.useid.transactioninfo

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("transaction_info")
data class TransactionInfo(
    @Column("useid_session_id")
    val useidSessionId: UUID,

    @Column("provider_name")
    val providerName: String,

    @Column("provider_url")
    val providerURL: String,

    @Column("additional_information")
    val additionalInformation: String
) {
    @Id
    var id: Long? = null

    @Column("created_at")
    @CreatedDate
    var createdAt: LocalDateTime? = null

    @Column("updated_at")
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
}
