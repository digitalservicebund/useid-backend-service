package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.apikeys.ApiKeyAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TenantIdFilter(
    private val tenantIdProperties: TenantIdProperties,
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val tenantId: String
        val authentication: Authentication? = SecurityContextHolder.getContext()?.authentication

        tenantId = if (authentication is ApiKeyAuthenticationToken && authentication.isAuthenticated) {
            authentication.details.tenantId
        } else if (request.servletPath.equals("/widget")) {
            tenantIdProperties.getTenantIdForHost(request.getParameter("hostname"))
        } else if (request.getParameter("tenant_id") != null) {
            tenantIdProperties.getSanitizedTenantID(request.getParameter("tenant_id"))
        } else {
            "unknown"
        }

        request.setAttribute("tenantId", tenantId)
        return filterChain.doFilter(request, response)
    }
}
