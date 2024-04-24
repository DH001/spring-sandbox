package dev.davidhiggins.springsandbox.audit

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.*
import java.time.Instant

@Document(indexName = "audit", createIndex = true)
@Setting(shards = 3, refreshInterval = "10s")
data class Audit(
    @Id
    val id: String,
    @Field(type = FieldType.Keyword)
    val action: AuditAction,
    @Field(type = FieldType.Keyword)
    val userId: String,
    @Field(type = FieldType.Date, dynamic = Dynamic.STRICT, format = [DateFormat.basic_date_time])
    val timestamp: Instant
)


enum class AuditAction {
    CREATE
}
