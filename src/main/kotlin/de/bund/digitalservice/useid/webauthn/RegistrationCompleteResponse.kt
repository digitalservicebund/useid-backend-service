package de.bund.digitalservice.useid.webauthn

class RegistrationCompleteResponse constructor(
    var rawId: String,
    var clientDataJSON: String,
    var attestationObject: String,
    var refreshAddress: String
)
