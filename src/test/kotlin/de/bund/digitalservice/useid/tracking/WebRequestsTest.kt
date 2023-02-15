// package de.bund.digitalservice.useid.tracking
//
// import io.mockk.every
// import io.mockk.mockk
// import io.mockk.verify
// import org.junit.jupiter.api.Assertions.assertEquals
// import org.junit.jupiter.api.BeforeEach
// import org.junit.jupiter.api.Tag
// import org.junit.jupiter.api.Test
// import org.springframework.http.ResponseEntity
// import org.springframework.web.reactive.function.client.WebClient
// import reactor.core.publisher.Mono
// import java.net.URI
//
// @Tag("test")
// class WebRequestsTest {
//
//     private val mockClient = mockk<WebClient>()
//     private val webRequests = WebRequests()
//
//     private val mockRequestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
//     private val mockRequestBodySpec = mockk<WebClient.RequestBodySpec>()
//     private val mockResponseSpec = mockk<WebClient.ResponseSpec>()
//
//     private var url = "https://example.com"
//
//     @BeforeEach
//     fun setup() {
//         // mock POST flow
//         every { mockClient.post() } returns mockRequestBodyUriSpec
//         every { mockRequestBodyUriSpec.uri(URI(url)) } returns mockRequestBodySpec
//         every { mockRequestBodySpec.retrieve() } returns mockResponseSpec
//     }
//
//     @Test
//     fun `POST request should return true when request was successful`() {
//         // GIVEN
//         every { mockResponseSpec.toBodilessEntity() } returns Mono.just(ResponseEntity.status(200).build())
//
//         // WHEN
//         val succeeded = webRequests.POST(url)
//
//         // THEN
//         assertEquals(true, succeeded)
//         verifyPostFlow()
//     }
//
//     @Test
//     fun `POST request should return false when error occurred`() {
//         // GIVEN
//         every { mockResponseSpec.toBodilessEntity() } returns Mono.error(Error())
//
//         // WHEN
//         val failed = webRequests.POST(url)
//
//         // THEN
//         assertEquals(false, failed)
//         verifyPostFlow()
//     }
//
//     private fun verifyPostFlow() {
//         verify { mockClient.post() }
//         verify { mockRequestBodyUriSpec.uri(URI(url)) }
//         verify { mockRequestBodySpec.retrieve() }
//         verify { mockResponseSpec.toBodilessEntity() }
//     }
// }
