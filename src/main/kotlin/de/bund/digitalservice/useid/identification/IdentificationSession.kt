package de.bund.digitalservice.useid.identification

import org.hibernate.annotations.Type
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "identification_session")
@Entity
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
