package dev.davidhiggins.springsandbox

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@SpringBootApplication
@EnableRetry
@EnableAsync
@EnableCaching
class Application

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}

@ControllerAdvice
class RestResponseEntityExceptionHandler: ResponseEntityExceptionHandler() {
	@ExceptionHandler(value = [IllegalArgumentException::class])
	protected fun handle(ex: RuntimeException, request: WebRequest): ResponseEntity<Any>? {
		return handleExceptionInternal(ex, "Unhandled error", HttpHeaders(), HttpStatus.CONFLICT, request)
	}
}
