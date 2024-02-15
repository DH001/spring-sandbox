package dev.davidhiggins.springsandbox.customer

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class CustomerCreationEventListener {
    private val log = LoggerFactory.getLogger(this::class.java)

    @EventListener
    @Async
    fun doEvent(event: Customer) {
        log.info("Received event for new customer: {}. Do something...", event.name)
    }
}