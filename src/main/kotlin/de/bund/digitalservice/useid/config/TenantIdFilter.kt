package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.apikeys.ApiKeyAuthenticationToken
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TenantIdFilter(
    private val contentSecurityPolicyProperties: ContentSecurityPolicyProperties
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        // Retrieve tenant id either from authenticated security context (api keys) or from url.
        // see the other two requests filters for more information
        // We still need a data structure that allows to retrieve the tenant id for a given value
        //
        // For some reason, we cannot rely on authenticated. It seems to be always set to true. Therefor we rely on the
        // authentication class.
        val tenantId: String
        val authentication: Authentication? = SecurityContextHolder.getContext()?.authentication

        tenantId = if (authentication is ApiKeyAuthenticationToken && authentication.isAuthenticated) {
            // api call with authentication token
            authentication.details.tenantId
        } else if (authentication is AnonymousAuthenticationToken) {
            // not an authenticated call or a failed api token authentication call
            // get tenant id based on csp header?
            contentSecurityPolicyProperties.getTenantId(request.getParameter("hostname"))
        } else {
            "unknown-call"
        }

        request.setAttribute("tenantId", tenantId)
        return filterChain.doFilter(request, response)
    }
}
