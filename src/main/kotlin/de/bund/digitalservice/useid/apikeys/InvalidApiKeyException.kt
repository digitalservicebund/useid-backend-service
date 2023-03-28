package de.bund.digitalservice.useid.apikeys

class InvalidApiKeyException(message: String) : Exception("API key is invalid: $message")
