// package de.bund.digitalservice.useid.apikeys
//
// import io.mockk.every
// import io.mockk.mockk
// import org.junit.jupiter.api.Tag
// import org.junit.jupiter.api.Test
// import org.junit.jupiter.params.ParameterizedTest
// import org.junit.jupiter.params.provider.Arguments
// import org.junit.jupiter.params.provider.MethodSource
// import org.springframework.http.HttpHeaders
// import org.springframework.http.server.reactive.ServerHttpRequest
// import org.springframework.web.server.ServerWebExchange
// import reactor.test.StepVerifier
// import java.util.stream.Stream
//
// private const val API_KEY = "some-api-key"
//
// @Tag("test")
// internal class ApiKeyAuthenticationConverterTest {
//     private val authenticationConverter: ApiKeyAuthenticationConverter = ApiKeyAuthenticationConverter()
//
//     @Test
//     fun `convert returns authentication token if authorization header is valid`() {
//         // Given
//         val serverWebExchange = mockk<ServerWebExchange>()
//         val serverHttpRequest = mockk<ServerHttpRequest>()
//         val httpHeaders = HttpHeaders()
//         httpHeaders.setBearerAuth(API_KEY)
//
//         every { serverWebExchange.request } returns serverHttpRequest
//         every { serverHttpRequest.headers } returns httpHeaders
//
//         // When
//         val authenticationMono = authenticationConverter.convert(serverWebExchange)
//
//         // Then
//         StepVerifier.create(authenticationMono)
//             .expectNextMatches {
//                 it.principal.equals(API_KEY) &&
//                     it.details is ApiKeyDetails &&
//                     (it.details as ApiKeyDetails).keyValue == API_KEY &&
//                     (it.details as ApiKeyDetails).refreshAddress == null &&
//                     (it.details as ApiKeyDetails).requestDataGroups == emptyList<String>() &&
//                     !it.isAuthenticated
//             }
//             .verifyComplete()
//     }
//
//     @ParameterizedTest
//     @MethodSource("invalidHeaders")
//     fun `convert returns empty if authorization header is missing`(headerValue: String?) {
//         // Given
//         val serverWebExchange = mockk<ServerWebExchange>()
//         val serverHttpRequest = mockk<ServerHttpRequest>()
//         val httpHeaders = HttpHeaders()
//         if (headerValue != null) {
//             httpHeaders.set(HttpHeaders.AUTHORIZATION, "$headerValue")
//         }
//
//         every { serverWebExchange.request } returns serverHttpRequest
//         every { serverHttpRequest.headers } returns httpHeaders
//
//         // When
//         val authenticationMono = authenticationConverter.convert(serverWebExchange)
//
//         // Then
//         StepVerifier.create(authenticationMono)
//             .expectNextCount(0)
//             .verifyComplete()
//     }
//
//     private fun invalidHeaders(): Stream<Arguments?>? {
//         return Stream.of(
//             Arguments.of(null), // no header
//             Arguments.of(""), // empty header
//             Arguments.of("wrong prefix"), // wrong prefix
//             Arguments.of("Bearer") // empty api key
//         )
//     }
// }
