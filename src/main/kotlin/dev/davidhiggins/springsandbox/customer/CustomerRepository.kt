package dev.davidhiggins.springsandbox.customer

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository

const val CUSTOMER_CACHE_KEY = "customers"

@Repository
interface CustomerRepository: ListCrudRepository<Customer, Int> {

    @Cacheable(CUSTOMER_CACHE_KEY)
    fun findByName(name: String): Customer

    @CacheEvict(cacheNames = [CUSTOMER_CACHE_KEY], key = "#result.id")
    @Override
    override fun <S : Customer> save(entity: S): S

}