package de.bund.digitalservice.useid.identification

import io.hypersistence.utils.hibernate.type.array.ListArrayType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

@Table(name = "identification_session")
@Entity
@EntityListeners(AuditingEntityListener::class)
data class IdentificationSession(
    @Column(name = "useid_session_id")
    var useIdSessionId: UUID? = null,

    @Column(name = "refresh_address")
    var refreshAddress: String? = null,

    @Column(name = "request_data_groups")
    @Type(ListArrayType::class)
    var requestDataGroups: List<String> = emptyList(),

    @Column(name = "tenant_id")
    var tenantId: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "eid_session_id")
    var eIdSessionId: UUID? = null

    @Column(name = "created_at")
    @CreatedDate
    var createdAt: LocalDateTime? = null

    @Column(name = "updated_at")
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
}
