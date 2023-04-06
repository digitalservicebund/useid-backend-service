package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.tenant.REQUEST_ATTR_TENANT
import de.bund.digitalservice.useid.tenant.Tenant
import de.bund.digitalservice.useid.widget.FALLBACK_PAGE
import de.bund.digitalservice.useid.widget.WIDGET_PAGE
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.server.PathContainer
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.pattern.PathPatternParser
import java.util.UUID

internal const val HTTP_HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy"
internal const val REQUEST_ATTR_CSP_NONCE = "cspNonce"
class WidgetSecurityHeadersFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val cspNonce = UUID.randomUUID().toString()
        request.setAttribute(REQUEST_ATTR_CSP_NONCE, cspNonce)

        if (requestPathMatches(WIDGET_PAGE, request)) {
            val tenant = request.getAttribute(REQUEST_ATTR_TENANT) as Tenant
            response.setHeader(HTTP_HEADER_CONTENT_SECURITY_POLICY, WidgetContentSecurityPolicy.headerValue(tenant.cspHost, cspNonce))
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, tenant.cspHost)
            response.setHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN)
        } else if (requestPathMatches(FALLBACK_PAGE, request)) {
            response.setHeader(HTTP_HEADER_CONTENT_SECURITY_POLICY, WidgetContentSecurityPolicy.headerValueNoFrame(cspNonce))
        }

        return filterChain.doFilter(request, response)
    }

    private fun requestPathMatches(path: String, request: HttpServletRequest): Boolean {
        return PathPatternParser().parse("/$path").matches(PathContainer.parsePath(request.servletPath))
    }
}
