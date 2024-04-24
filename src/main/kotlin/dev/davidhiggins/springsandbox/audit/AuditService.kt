package dev.davidhiggins.springsandbox.audit

import dev.davidhiggins.springsandbox.customer.Customer
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import kotlin.streams.asSequence

@Component
class AuditService(
    private val repository: AuditRepository
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun searchByRange(from: Instant, to: Instant): Sequence<Audit> = repository.findAllByTimestampBetween(from, to).asSequence()

    @EventListener
    @Async
    fun doEvent(customer: Customer) {
        val audit = Audit(
            id = UUID.randomUUID().toString(),
            userId = customer.id.toString(),
            timestamp = Instant.now(),
            action = AuditAction.CREATE
        )
        runCatching {
            repository.save(audit)
        }.onFailure { log.error("Failed to write audit record: $audit", it) }
    }
}