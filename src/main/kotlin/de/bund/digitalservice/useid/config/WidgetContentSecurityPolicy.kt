package de.bund.digitalservice.useid.config

import org.springframework.stereotype.Component

const val CSP_DEFAULT_CONFIG = "default-src 'self';style-src 'self';font-src 'self';img-src 'self';connect-src 'self'"
const val CSP_FRAME_ANCESTORS_SELF = "frame-ancestors 'self'"
const val CSP_FRAME_ANCESTORS_NONE = "frame-ancestors 'none'"

@Component
class WidgetContentSecurityPolicy {

    companion object {
        fun headerValue(host: String, nonce: String): String {
            return "$CSP_DEFAULT_CONFIG;${headerValueScript(nonce)};$CSP_FRAME_ANCESTORS_SELF $host;"
        }
        fun headerValueNoFrame(nonce: String): String {
            return "$CSP_DEFAULT_CONFIG;${headerValueScript(nonce)};$CSP_FRAME_ANCESTORS_NONE;"
        }
        private fun headerValueScript(nonce: String): String {
            return "script-src 'self' 'nonce-$nonce'"
        }
    }
}
