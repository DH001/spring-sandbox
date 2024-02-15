package dev.davidhiggins.springsandbox.customer

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class CustomerMessageConsumer(private val customerService: CustomerService) {
    @RabbitListener(queues = ["\${rabbitmq.queue.name}"])
    fun consume(message: CreateCustomerRequest) {
        customerService.create(message)
    }
}