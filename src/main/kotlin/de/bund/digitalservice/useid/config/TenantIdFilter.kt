package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.apikeys.ApiKeyAuthenticationToken
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class TenantIdFilter(
    private val tenantProperties: TenantProperties,
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val authentication: Authentication? = SecurityContextHolder.getContext()?.authentication

        val tenant: Tenant? = if (authentication is ApiKeyAuthenticationToken && authentication.isAuthenticated) {
            tenantProperties.findByApiKey(authentication.details.keyValue)
        } else if (request.servletPath.equals("/widget")) {
            tenantProperties.findByAllowedHost(request.getParameter("hostname"))
        } else if (request.getParameter("tenant_id") != null) {
            tenantProperties.findByAllowedHost(request.getParameter("tenant_id"))
        } else {
            val defaultTenant = Tenant()
            defaultTenant.id = "default"
            defaultTenant
        }

        request.setAttribute("tenant", tenant)
        return filterChain.doFilter(request, response)
    }
}
