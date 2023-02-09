package de.bund.digitalservice.useid.apikeys

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private const val AUTH_HEADER_VALUE_PREFIX = "Bearer "

class ApiKeyAuthenticationFilter(private val authenticationManager: AuthenticationManager) :
    AbstractAuthenticationProcessingFilter("/**") { // TODO

    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authHeader.trim().isEmpty() || !authHeader.trim().startsWith(AUTH_HEADER_VALUE_PREFIX)) {
            throw BadCredentialsException("API Key is missing.")
        }
        val token = authHeader.substring(AUTH_HEADER_VALUE_PREFIX.length)
        return authenticationManager.authenticate(ApiKeyAuthenticationToken(token))
    }

    // @Throws(IOException::class, ServletException::class)
    // override fun successfulAuthentication(
    //     request: HttpServletRequest?,
    //     response: HttpServletResponse?,
    //     chain: FilterChain,
    //     authResult: Authentication?
    // ) {
    //     SecurityContextHolder.getContext().authentication = authResult
    //     chain.doFilter(request, response)
    // }
}
