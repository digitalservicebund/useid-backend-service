package de.bund.digitalservice.useid.eidservice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EidServicePropertiesTest {

    @Test
    fun getKeystorePassword() {
        val eidServiceProps = EidServiceProperties()
        eidServiceProps.keystorePassword = "test_"
        assertEquals(eidServiceProps.keystorePassword, "test_")
    }
}
