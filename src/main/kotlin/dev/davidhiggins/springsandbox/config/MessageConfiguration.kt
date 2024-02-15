package dev.davidhiggins.springsandbox.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class MessageConfiguration {

    @Bean
    fun queue(@Value("\${rabbitmq.queue.name}") queueName: String)
        = Queue(queueName, false)

    @Bean
    fun exchange(@Value("\${rabbitmq.exchange.name}") exchange: String)
        = TopicExchange(exchange)

    @Bean
    fun binding(
        queue: Queue,
        exchange: TopicExchange,
        @Value("\${rabbitmq.routing.key}") routingKey: String
    ): Binding = BindingBuilder.bind(queue).to(exchange).with(routingKey)

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = messageConverter()
        return rabbitTemplate
    }

    @Bean
    fun messageConverter(): Jackson2JsonMessageConverter {
        return Jackson2JsonMessageConverter()
    }
}

