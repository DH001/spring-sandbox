package dev.davidhiggins.springsandbox

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dev.davidhiggins.springsandbox.customer.CreateCustomerRequest
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component


@Component
class TestMessageSender(
    @Value("\${rabbitmq.exchange.name}")
    private val exchange: String,
    @Value("\${rabbitmq.routing.key}")
    private val routingKey: String,
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper) : CommandLineRunner {



    override fun run(vararg args: String) {
        val json = objectMapper.valueToTree<JsonNode>(CreateCustomerRequest("testFromMsg"))
        println("Sending message: $json")
        rabbitTemplate.convertAndSend(exchange, routingKey, json)
    }
}
