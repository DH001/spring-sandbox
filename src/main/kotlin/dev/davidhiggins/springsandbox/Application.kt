package dev.davidhiggins.springsandbox

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.EnableRetry
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@SpringBootApplication
@EnableRetry
class Application

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}


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

@ControllerAdvice
class RestResponseEntityExceptionHandler: ResponseEntityExceptionHandler() {
	@ExceptionHandler(value = [IllegalArgumentException::class])
	protected fun handle(ex: RuntimeException, request: WebRequest): ResponseEntity<Any>? {
		return handleExceptionInternal(ex, "Unhandled error", HttpHeaders(), HttpStatus.CONFLICT, request)
	}
}
