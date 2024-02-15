package dev.davidhiggins.springsandbox.customer

import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository: ListCrudRepository<Customer, String>