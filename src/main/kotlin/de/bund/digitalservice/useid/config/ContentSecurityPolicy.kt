package de.bund.digitalservice.useid.config

import org.springframework.stereotype.Component

internal const val CSP_DEFAULT_CONFIG = "default-src 'self';script-src 'self';style-src 'self';font-src 'self';img-src 'self';connect-src 'self';"
internal const val CSP_FRAME_ANCESTORS = "frame-ancestors 'self'"

@Component
class ContentSecurityPolicy {

    fun getCSPHeaderValue(host: String): String {
        return "$CSP_DEFAULT_CONFIG$CSP_FRAME_ANCESTORS $host;"
    }

    fun getDefaultCSPHeaderValue(): String {
        return "$CSP_DEFAULT_CONFIG$CSP_FRAME_ANCESTORS;"
    }
}
