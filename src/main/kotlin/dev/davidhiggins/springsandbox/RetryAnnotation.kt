package dev.davidhiggins.springsandbox

import org.slf4j.LoggerFactory
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Retryable(
    maxAttempts = 3,
    include = [ResourceAccessException::class],
    backoff = Backoff(delay = 200L),
    listeners = ["retryLogListener"]
)
annotation class ResourceAccessRetryable

@Component
class RetryLogListener: RetryListener {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun<T, E: Throwable> onError(
        context: RetryContext,
        callback: RetryCallback<T, E>?,
        throwable: Throwable
    ) {
        log.warn("Error triggered a retry", throwable)
        super.onError(context, callback, throwable)
    }
}