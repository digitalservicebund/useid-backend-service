package de.bund.digitalservice.useid.transactioninfo

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("additional_info")
data class AdditionalInfoDto(
    @Column("useid_session_id")
    val useIdSessionId: UUID,

    @Column("key")
    val key: String,

    @Column("value")
    val value: String
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
