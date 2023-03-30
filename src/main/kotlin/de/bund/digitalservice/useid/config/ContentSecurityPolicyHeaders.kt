package de.bund.digitalservice.useid.config

import org.springframework.stereotype.Component

internal const val CSP_DEFAULT_CONFIG = "default-src 'self';style-src 'self';font-src 'self';img-src 'self';connect-src 'self'"
internal const val CSP_SCRIPT_SRC_CONFIG = "script-src 'self'"
internal const val CSP_FRAME_ANCESTORS = "frame-ancestors 'self'"

@Component
abstract class ContentSecurityPolicyHeaders {

    companion object {

        const val default = "$CSP_DEFAULT_CONFIG;$CSP_FRAME_ANCESTORS;"
        fun widget(host: String, nonce: String): String {
            return "$CSP_DEFAULT_CONFIG;$CSP_SCRIPT_SRC_CONFIG 'nonce-$nonce';$CSP_FRAME_ANCESTORS $host;"
        }
    }
}
