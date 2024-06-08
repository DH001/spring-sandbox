package dev.davidhiggins.springsandbox.customer

import dev.davidhiggins.springsandbox.customer.client.ContactDetails
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api")
@Validated
class CustomerController(private val customerService: CustomerService) {

    @GetMapping("/customer/{id}")
    fun getById(
        @PathVariable("id") id: Int
    ): Customer = customerService.findById(id)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Customer Not Found")

    @GetMapping("/customer/{id}/contact")
    fun getContactDetails(
        @PathVariable("id") id: Int
    ): ContactDetails = customerService.getCustomerContactDetails(id)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Customer Not Found")

    @GetMapping("/customer")
    fun getAll(): List<Customer> = customerService.findAll()

    @PostMapping("/customer")
    @ResponseStatus(value = HttpStatus.CREATED)
    fun create(@RequestBody request: CreateCustomerRequest): Customer {
       return customerService.create(request)
    }

    @DeleteMapping("/customer/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    fun delete(@PathVariable("id") id: Int) = customerService.delete(id)

}

data class CreateCustomerRequest(
    @field:NotBlank(message = "Name must not be blank")
    val name: String
)

