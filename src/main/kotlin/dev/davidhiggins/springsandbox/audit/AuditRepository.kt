package dev.davidhiggins.springsandbox.audit

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.stream.Stream

@Repository
interface AuditRepository: ElasticsearchRepository<Audit, String> {

    fun findAllByTimestampBetween(from: Instant, to: Instant): Stream<Audit>

}

