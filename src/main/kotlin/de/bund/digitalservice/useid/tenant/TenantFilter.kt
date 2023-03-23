package de.bund.digitalservice.useid.tenant

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class TenantFilter(
    private val tenantProperties: TenantProperties,
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val tenant: Tenant? = if (request.servletPath.equals("/widget")) {
            tenantProperties.findByAllowedHost(request.getParameter("hostname"))
        } else if (request.getParameter("tenant_id") != null) {
            tenantProperties.findByTenantId(request.getParameter("tenant_id"))
        } else {
            val defaultTenant = Tenant()
            defaultTenant.id = "unknown"
            defaultTenant
        }

        request.setAttribute("tenant", tenant)
        return filterChain.doFilter(request, response)
    }
}
