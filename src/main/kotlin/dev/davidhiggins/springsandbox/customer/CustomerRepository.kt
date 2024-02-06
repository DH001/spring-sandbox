package dev.davidhiggins.springsandbox.customer

import dev.davidhiggins.springsandbox.customer.service.Customer
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository: ListCrudRepository<Customer, String>