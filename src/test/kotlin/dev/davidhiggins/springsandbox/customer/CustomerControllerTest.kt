package dev.davidhiggins.springsandbox.customer

import com.ninjasquad.springmockk.MockkBean
import dev.davidhiggins.springsandbox.config.ApplicationConfig
import dev.davidhiggins.springsandbox.customer.client.Address
import dev.davidhiggins.springsandbox.customer.client.ContactDetails
import dev.davidhiggins.springsandbox.customer.client.ContactDetailsClient
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*


@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = [ApplicationConfig::class, CustomerController::class, CustomerService::class])
//@TestPropertySource(properties = [])
class CustomerControllerTest {

    @MockkBean
    lateinit var customerRepository: CustomerRepository

    @MockkBean
    lateinit var contactDetailsClient: ContactDetailsClient

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `Get a customer by their id`() {
        every { customerRepository.findById("1") } returns Optional.of(Customer(1, "test"))

        val result = mockMvc.perform(
            get("/api/customer/1")
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(status().`is`(200))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""{ "id": 1, "name": "test" }""".trimIndent()))
    }

    @Test
    fun `Get a customer but they are not found`() {
        every { customerRepository.findById("1") } returns Optional.empty()

        val result = mockMvc.perform(
            get("/api/customer/1")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        ).andDo(MockMvcResultHandlers.print())

        result.andExpect(status().`is`(404))
    }

    @Test
    fun `Fetch contact details for customer from a web service`() {
        every { customerRepository.findById("1") } returns Optional.of(Customer(1, "test"))
        every { contactDetailsClient.getContactDetails(1) } returns ContactDetails(
            id = 1,
            email = "test@test.com",
            phone = "555",
            address = Address(
                street = "1 Street",
                city = "Place",
                postcode = "AA1"
            )
        )

        val result = mockMvc.perform(
            get("/api/customer/1/contact")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(status().`is`(200))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(
                """{ 
                    "id": 1, 
                    "email": "test@test.com", 
                    "phone": "555",
                    "address": {
                      "street": "1 Street", "city": "Place", "postcode": "AA1"
                    }
                }""".trimIndent()))
    }

    @Test
    fun `Get all customers in a list`() {
        every { customerRepository.findAll() } returns listOf(Customer(1, "test"))

        val result = mockMvc.perform(
            get("/api/customer")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        ).andDo(MockMvcResultHandlers.print())

        result
            .andExpect(status().`is`(200))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("""[{ "id": 1, "name": "test" }]""".trimIndent()))
    }

    @Test
    fun `Create a new customer with specified name`() {
        every { customerRepository.save(any()) } returnsArgument 0

        val result = mockMvc.perform(
            post("/api/customer")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .content("""{ "name": "test1" }""")
        ).andDo(MockMvcResultHandlers.print())

        result.andExpect(status().`is`(201))
        verify { customerRepository.save(Customer(null, "test1")) } // Id created by datastore
    }

    @Test
    fun `Delete a user by id`() {
        every { customerRepository.deleteById(any()) } just runs

        val result = mockMvc.perform(
            delete("/api/customer/1")
        ).andDo(MockMvcResultHandlers.print())

        result.andExpect(status().`is`(204))
        verify { customerRepository.deleteById("1") }
    }
}