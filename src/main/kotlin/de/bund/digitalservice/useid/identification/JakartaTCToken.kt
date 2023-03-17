package de.bund.digitalservice.useid.identification

import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlSchemaType
import jakarta.xml.bind.annotation.XmlType
import jakarta.xml.bind.annotation.adapters.HexBinaryAdapter
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter

/**
 * JakartaTCToken is an alternative to TCTokenType (which uses javax annotations for JAXB) using jakarta JAXB annotations
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "",
    propOrder = ["serverAddress", "sessionIdentifier", "refreshAddress", "communicationErrorAddress", "binding", "pathSecurityProtocol", "pathSecurityParameters", "levelOfAssurance"],
)
@XmlRootElement(name = "TCTokenType")
open class JakartaTCToken {

    @XmlElement(name = "ServerAddress", required = true)
    @XmlSchemaType(name = "anyURI")
    protected var serverAddress: String? = null

    @XmlElement(name = "SessionIdentifier", required = true)
    protected var sessionIdentifier: String? = null

    @XmlElement(name = "RefreshAddress", required = true)
    @XmlSchemaType(name = "anyURI")
    protected var refreshAddress: String? = null

    @XmlElement(name = "CommunicationErrorAddress")
    @XmlSchemaType(name = "anyURI")
    protected var communicationErrorAddress: String? = null

    @XmlElement(name = "Binding", required = true)
    @XmlSchemaType(name = "anyURI")
    protected var binding: String? = null

    @XmlElement(name = "PathSecurity-Protocol")
    @XmlSchemaType(name = "anyURI")
    protected var pathSecurityProtocol: String? = null

    @XmlElement(name = "PathSecurity-Parameters")
    protected var pathSecurityParameters: PathSecurityParameters? = null

    @XmlElement(name = "LevelOfAssurance")
    protected var levelOfAssurance: String? = null

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = ["psk"])
    class PathSecurityParameters(
        @field:XmlElement(name = "PSK", type = String::class)
        @field:XmlJavaTypeAdapter(
            HexBinaryAdapter::class,
        )
        @field:XmlSchemaType(name = "hexBinary")
        var psk: ByteArray,
    )

    companion object {
        fun fromTCTokenType(token: TCTokenType): JakartaTCToken = JakartaTCToken().apply {
            serverAddress = token.serverAddress
            sessionIdentifier = token.sessionIdentifier
            refreshAddress = token.refreshAddress
            communicationErrorAddress = token.communicationErrorAddress
            binding = token.binding
            pathSecurityProtocol = token.pathSecurityProtocol
            pathSecurityParameters =
                if (token.pathSecurityParameters != null) PathSecurityParameters(token.pathSecurityParameters.psk) else null
            levelOfAssurance = token.levelOfAssurance
        }
    }
}
