package dev.davidhiggins.springsandbox.customer.client

import com.fasterxml.jackson.annotation.JsonAlias
import dev.davidhiggins.springsandbox.ResourceAccessRetryable
import dev.davidhiggins.springsandbox.config.baseWebClientBuilder
import dev.davidhiggins.springsandbox.config.getOrNull
import io.github.resilience4j.ratelimiter.annotation.RateLimiter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class ContactDetailsClient(
    @Value("\${contact.service.url}") baseUrl: String
){
    private val log: Logger = LoggerFactory.getLogger(WebClient::class.java)

    private val webClient = baseWebClientBuilder(log)
        .baseUrl(baseUrl)
        .build()

    @ResourceAccessRetryable
    @RateLimiter(name = "getContactDetails")
    fun getContactDetails(id: Int): ContactDetails? = webClient.getOrNull<ContactDetails>("/$id", log)

}

/**
 * {
 *   "id": 1,
 *   "email": "",
 *   "address": {
 *     "street": "",
 *     "suite": "",
 *     "city": "",
 *     "zipcode": ""
 *   },
 *   "phone": "",
 *   "website": "",
 *   "company": {
 *     "name": ""
 *   }
 * }
 */
data class ContactDetails(
    val id: Int,
    val email: String?,
    val address: Address?,
    val phone: String?
)

data class Address(
    val street: String?,
    val city: String?,
    @JsonAlias("zipcode")
    val postcode: String?
)

