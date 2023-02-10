package de.bund.digitalservice.useid.apikeys

import de.bund.digitalservice.useid.identification.IDENTIFICATION_SESSIONS_BASE_PATH
import org.springframework.http.HttpHeaders
import org.springframework.http.server.PathContainer
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.web.util.pattern.PathPatternParser
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private const val AUTH_HEADER_VALUE_PREFIX = "Bearer "

class ApiKeyAuthenticationFilter(private val authenticationManager: AuthenticationManager, pathPattern: String) :
    AbstractAuthenticationProcessingFilter(pathPattern) {

    override fun requiresAuthentication(request: HttpServletRequest?, response: HttpServletResponse?): Boolean {
        val listOfPages = listOf(
            PathPatternParser().parse("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tc-token"),
            PathPatternParser().parse("/actuator/health"),
            PathPatternParser().parse("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tokens/*"),
            PathPatternParser().parse("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tokens"),
            PathPatternParser().parse("$IDENTIFICATION_SESSIONS_BASE_PATH/*/transaction-info") // TODO: GET should not be authenticated, but POST should be
        )
        val permittedPath = listOfPages.any {
            it.matches(PathContainer.parsePath(request!!.servletPath))
        }

        if (permittedPath) {
            return false
        }

        return super.requiresAuthentication(request, response)
    }
    override fun attemptAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Authentication {
        val listOfPages = listOf(
            PathPatternParser().parse("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tc-token"),
            PathPatternParser().parse("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tokens/*"),
            PathPatternParser().parse("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tokens"),
            PathPatternParser().parse("$IDENTIFICATION_SESSIONS_BASE_PATH/*/transaction-info") // TODO: GET should not be authenticated, but POST should be
        )
        val pathShouldNotBeAuthenticated = listOfPages.any {
            it.matches(PathContainer.parsePath(request.servletPath))
        }
        if (pathShouldNotBeAuthenticated) {
            // throw BadCredentialsException("STUFF")
            return ApiKeyAuthenticationToken("")
        }
        val authHeader: String? = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authHeader == null || authHeader.trim().isEmpty() || !authHeader.trim().startsWith(AUTH_HEADER_VALUE_PREFIX)) {
            throw BadCredentialsException("API Key is missing.")
        }
        val token = authHeader.substring(AUTH_HEADER_VALUE_PREFIX.length)
        return authenticationManager.authenticate(ApiKeyAuthenticationToken(token))
    }
}
