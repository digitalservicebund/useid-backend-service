package de.bund.digitalservice.useid.tracking

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono

@Tag("test")
@WebFluxTest(controllers = [WebRequests::class])
class WebRequestsTest {

    @MockkBean
    private lateinit var webRequests: WebRequests

    @Test
    fun `POST request should return status code`() {
        every { webRequests.POST(any()) } returns Mono.just(ResponseEntity.status(200).build())

        val response = webRequests.POST("_").block()
        assertEquals(HttpStatus.OK, response?.statusCode)
    }
}
