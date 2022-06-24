package de.bund.digitalservice.useid.events

class Event(val consumerId: String, val encryptedRefreshAddress: String, val widgetSessionId: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        if (consumerId != other.consumerId) return false
        if (encryptedRefreshAddress != other.encryptedRefreshAddress) return false
        if (widgetSessionId != other.widgetSessionId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = consumerId.hashCode()
        result = 31 * result + encryptedRefreshAddress.hashCode()
        result = 31 * result + widgetSessionId.hashCode()
        return result
    }
}