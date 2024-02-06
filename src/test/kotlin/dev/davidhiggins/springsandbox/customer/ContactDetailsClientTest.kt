package dev.davidhiggins.springsandbox.customer

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

private const val TEST_PORT = 8989

class ContactDetailsClientTest {
    private val mockServer = MockWebServer()
    private val client = ContactDetailsClient("http://localhost:$TEST_PORT")

    @BeforeEach
    fun setup() = mockServer.start(TEST_PORT)

    @AfterEach
    fun teardown() = mockServer.shutdown()

    @Test
    fun `Get user contact details successfully`() {
        mockServer.enqueue(
            MockResponse().apply {
                setResponseCode(200)
                setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                setBody(
                    """
                     {
                          "id": 1,
                          "email": "test@test.com",
                          "address": {
                            "street": "1 High Street",
                            "city": "Bristol",
                            "zipcode": "BS1"
                          },
                          "phone": "555"
                     }
                    """.trimIndent()
                )
            }
        )

        assertThat(client.getContactDetails(1)).isEqualTo(
            ContactDetails(
                id = 1,
                email = "test@test.com",
                address = Address(
                    street = "1 High Street",
                    city = "Bristol",
                    postcode = "BS1",

                ),
                phone = "555"
            )
        )
    }

    @Test
    fun `User contact details not found so return null`() {
        mockServer.enqueue(
            MockResponse().apply {
                setResponseCode(404)
                setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            }
        )
        assertThat(client.getContactDetails(1)).isNull()
    }
}