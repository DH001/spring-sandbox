package dev.davidhiggins.springsandbox.customer

import dev.davidhiggins.springsandbox.Extensions.sanitize
import dev.davidhiggins.springsandbox.ResourceAccessRetryable
import dev.davidhiggins.springsandbox.customer.client.ContactDetailsClient
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

@Component
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val contactDetailsClient: ContactDetailsClient,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun findAll(): List<Customer> =
        customerRepository.findAll().map { it }.toList()

    @ResourceAccessRetryable
    fun findById(id: String): Customer? =
        customerRepository.findById(id)
            .getOrNull()

    fun create(request: CreateCustomerRequest): Customer {
        val customer = customerRepository.save(
            Customer(
                id = null,
                name = request.name.sanitize()
            )
        )
        eventPublisher.publishEvent(customer)
        log.info("Created new user: {}", customer.id)
        return customer
    }

    fun delete(id: String) =
        customerRepository.deleteById(id)
            .also { log.info("Deleted user with id: {}", id) }

    fun getCustomerContactDetails(id: String) =
        customerRepository.findById(id)
            .getOrNull()
            ?.let {  contactDetailsClient.getContactDetails(it.id!!) }

}
