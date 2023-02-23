package de.bund.digitalservice.useid.webauthn

class AuthenticatorAttestationResponse {
    lateinit var rawAttestationObject: String
    lateinit var rawClientDataJSON: String
    lateinit var credentialId: String
    lateinit var refreshAddress: String
}
