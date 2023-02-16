package de.bund.digitalservice.useid.identification

import io.hypersistence.utils.hibernate.type.array.ListArrayType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@TypeDefs(
    value = [
        TypeDef(name = "list-array", typeClass = ListArrayType::class)
    ]
)
@Table(name = "identification_session")
@Entity
@EntityListeners(AuditingEntityListener::class)
data class IdentificationSession(
    @Column(name = "useid_session_id")
    var useIdSessionId: UUID? = null,

    @Column(name = "refresh_address")
    var refreshAddress: String? = null,

    @Column(name = "request_data_groups")
    @Type(type = "list-array")
    var requestDataGroups: List<String> = emptyList()
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
