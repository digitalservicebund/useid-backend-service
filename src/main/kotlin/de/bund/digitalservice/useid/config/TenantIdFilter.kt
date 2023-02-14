package de.bund.digitalservice.useid.config

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Component
class TenantIdFilter : GenericFilterBean() {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        // Retrieve tenant id either from authenticated security context (api keys) or from url.
        // see the other two requests filters for more information
        // We still need a data structure that allows to retrieve the tenant id for a given value
        //
        // For now we always set the tenant id to unknown
        val tenantId: String
        val isAuthenticated: Boolean? = SecurityContextHolder.getContext()?.authentication?.isAuthenticated

        //TODO: This seems to be true for /actuator/health. We need a better way
        tenantId = if (isAuthenticated == true) {
            // api call with authentication token
            "unknown-api-call"
        } else {
            // not an authenticated call or a failed authentication call
            // get tenant id based on csp header?
            "client-call"
        }

        request.setAttribute("tenantId", tenantId)
        return chain.doFilter(request, response)
    }
}
