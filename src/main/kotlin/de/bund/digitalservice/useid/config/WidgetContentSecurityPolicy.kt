package de.bund.digitalservice.useid.config

import org.springframework.stereotype.Component

internal const val CSP_DEFAULT_CONFIG = "default-src 'self';style-src 'self';font-src 'self';img-src 'self';connect-src 'self'"
internal const val CSP_FRAME_ANCESTORS = "frame-ancestors 'self'"

@Component
class WidgetContentSecurityPolicy {

    companion object {
        fun headerValue(host: String, nonce: String): String {
            return "$CSP_DEFAULT_CONFIG;script-src 'self' 'nonce-$nonce';$CSP_FRAME_ANCESTORS $host;"
        }
    }
}
