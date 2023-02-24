package de.bund.digitalservice.useid.webauthn

class AuthenticationCompleteResponse constructor(
    var clientDataJSON: com.yubico.webauthn.data.ByteArray,
    var authenticatorData: com.yubico.webauthn.data.ByteArray,
    var signature: com.yubico.webauthn.data.ByteArray
)
