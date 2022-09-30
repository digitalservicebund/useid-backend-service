package de.bund.digitalservice.useid.identification

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("identification_session")
data class IdentificationSession(
    @Column("useid_session_id")
    val useidSessionId: UUID,

    @Column("refresh_address")
    val refreshAddress: String,

    @Column("request_data_groups")
    val requestDataGroups: List<String>
) {
    @Id
    var id: Long? = null

    @Column("eid_session_id")
    var eidSessionId: UUID? = null

    @Column("created_at")
    @CreatedDate
    var createdAt: LocalDateTime? = null

    @Column("updated_at")
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
}
