package dev.davidhiggins.springsandbox

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringSandboxApplication

fun main(args: Array<String>) {
	runApplication<SpringSandboxApplication>(*args)
}
