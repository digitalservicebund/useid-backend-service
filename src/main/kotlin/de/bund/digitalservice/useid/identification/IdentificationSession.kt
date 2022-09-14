package de.bund.digitalservice.useid.identification

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("identification_session")
data class IdentificationSession(
    @Column("useid_session_id")
    var useIDSessionId: UUID,

    @Column("refresh_address")
    var refreshAddress: String,

    @Column("request_data_groups")
    private var requestDataGroups: String
) {
    @Id
    var id: Long? = null

    @Column("eid_session_id")
    var eIDSessionId: UUID? = null

    constructor(useIDSessionId: UUID, refreshAddress: String, requestDataGroupList: List<String>) : this(
        useIDSessionId,
        refreshAddress,
        requestDataGroupList.joinToString(",")
    )

    fun getRequestedDataGroups(): List<String> {
        return requestDataGroups.split(",")
    }
}
