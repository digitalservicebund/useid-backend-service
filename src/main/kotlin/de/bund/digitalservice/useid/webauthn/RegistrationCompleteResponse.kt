package de.bund.digitalservice.useid.webauthn

class RegistrationCompleteResponse constructor(
    var rawId: com.yubico.webauthn.data.ByteArray,
    var clientDataJSON: com.yubico.webauthn.data.ByteArray,
    var attestationObject: com.yubico.webauthn.data.ByteArray,
    var refreshAddress: String
)
