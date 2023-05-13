package de.bund.digitalservice.useid.tracking

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers

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
        assertThat(succeeded).isEqualTo(true)
        verify {
            mockClient.send(
                withArg { request ->
                    assertThat(request.uri()).isEqualTo(URI(url))
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
        assertThat(failed).isEqualTo(false)
    }
}
