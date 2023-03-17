package de.bund.digitalservice.useid.tracking

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers

@Tag("test")
class WebRequestsTest {

    private val mockClient = mockk<HttpClient>()
    private val webRequests = WebRequests(mockClient)

    private var url = "https://example.com"

    @Test
    fun `POST request should return true when request to correct url was successful`() {
        // GIVEN
        val mockedResponse = mockk<HttpResponse<String>>()
        every { mockedResponse.statusCode() } returns HttpStatus.OK.value()
        every { mockClient.send(any(), BodyHandlers.ofString()) } returns mockedResponse

        // WHEN
        val succeeded = webRequests.POST(url)

        // THEN
        assertEquals(true, succeeded)
        verify {
            mockClient.send(
                withArg { request ->
                    assertEquals(URI(url), request.uri())
                },
                BodyHandlers.ofString(),
            )
        }
    }

    @Test
    fun `POST request should return false when error occurred`() {
        // GIVEN
        every { mockClient.send(any(), BodyHandlers.ofString()) } throws IOException()

        // WHEN
        val failed = webRequests.POST(url)

        // THEN
        assertEquals(false, failed)
    }
}
