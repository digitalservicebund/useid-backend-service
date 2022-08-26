package de.bund.digitalservice.useid.apikeys

import de.bund.digitalservice.useid.config.ApiProperties
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import reactor.test.StepVerifier
import java.util.stream.Stream

private const val API_KEY = "some-api-key"
private const val REFRESH_ADDRESS = "some-refresh-address"
private val DATA_GROUPS = listOf("DG1", "DG2")

@Tag("test")
private class ApiKeyAuthenticationManagerTest {

    private var apiProperties: ApiProperties = mockk()

    private val authenticationManager: ApiKeyAuthenticationManager = ApiKeyAuthenticationManager(apiProperties)

    @Test
    fun `authenticate returns authenticated token if api key is valid`() {
        // Given
        val unauthenticatedToken = ApiKeyAuthenticationToken(API_KEY)

        every { apiProperties.apiKeys } returns apiKeys()

        // When
        val authenticationMono = authenticationManager.authenticate(unauthenticatedToken)

        // Then
        StepVerifier.create(authenticationMono)
            .expectNextMatches {
                it.principal.equals(API_KEY) &&
                    it.details is ApiKeyDetails &&
                    (it.details as ApiKeyDetails).keyValue == API_KEY &&
                    (it.details as ApiKeyDetails).refreshAddress == REFRESH_ADDRESS &&
                    (it.details as ApiKeyDetails).requestDataGroups == DATA_GROUPS &&
                    it.isAuthenticated
            }
            .verifyComplete()
    }

    @ParameterizedTest
    @MethodSource("invalidAuthenticationArguments")
    fun `authenticate returns empty if api key is invalid`(authentication: Authentication) {
        // Given
        every { apiProperties.apiKeys } returns apiKeys()

        // When
        val authenticationMono = authenticationManager.authenticate(authentication)

        // Then
        StepVerifier.create(authenticationMono)
            .expectError(BadCredentialsException::class.java)
            .verify()
    }

    private fun invalidAuthenticationArguments(): Stream<Arguments?>? {
        return Stream.of(
            Arguments.of(ApiKeyAuthenticationToken("invalid-api-key")), // invalid api key
            Arguments.of(mockk<UsernamePasswordAuthenticationToken>()) // wrong authentication type
        )
    }

    private fun apiKeys(): List<ApiProperties.ApiKey> {
        val apiKey = ApiProperties.ApiKey()
        apiKey.keyValue = API_KEY
        apiKey.refreshAddress = REFRESH_ADDRESS
        apiKey.dataGroups = DATA_GROUPS
        return listOf(apiKey)
    }
}
