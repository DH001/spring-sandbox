package dev.davidhiggins.springsandbox.config

import dev.davidhiggins.springsandbox.customer.ContactDetails
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.slf4j.Logger
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

private val httpClient = HttpClient.create()
    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
    .responseTimeout(Duration.ofMillis(5000))
    .doOnConnected { conn ->
        conn.addHandlerLast(ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
            .addHandlerLast(WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS))
    }


fun baseWebClientBuilder(log: Logger) = WebClient.builder()
    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
    .clientConnector(ReactorClientHttpConnector(httpClient))
    .filters {
        it.add(LogFilter.logRequest(log))
        it.add(LogFilter.logResponse(log))
    }

inline fun <reified T> WebClient.getOrNull(url: String, log: Logger): T? =
    runCatching {
         this
            .get()
            .uri { it.path(url).build() }
            .retrieve()
            .onStatus({ it.value() == 404 }, { Mono.empty() })
            .bodyToMono(object : ParameterizedTypeReference<T>() {})
            .block()
    }
        .onFailure { e ->
            when (e) {
                is WebClientResponseException -> log.error("REST call failed with {}: {}", e.statusCode, e.statusText, e)
                else -> log.error("REST call failed", e)
            }
        }
        .getOrDefault(null)



private object LogFilter {
    fun logRequest(log: Logger): ExchangeFilterFunction = ExchangeFilterFunction.ofRequestProcessor { clientRequest ->
        if (log.isDebugEnabled) {
            val msg = buildString {
                append("curl -X ${clientRequest.method()} ")
                val headers = getRedactedHeaders(clientRequest.headers())
                if (headers.isNotEmpty()) append("-H ")
                headers.forEach {
                    append("${it.key} : ${it.value} ")
                }
                if (clientRequest.method() == HttpMethod.POST || clientRequest.method() == HttpMethod.PUT) {
                    append("-d '${clientRequest.body()}' ")
                }
                append("'${clientRequest.url()}' ")


            }
            log.debug(msg)
        }
        Mono.just(clientRequest)
    }

    fun logResponse(log: Logger): ExchangeFilterFunction = ExchangeFilterFunction.ofResponseProcessor { clientResponse ->
        log.debug("{}", clientResponse.statusCode())
        Mono.just(clientResponse);
    }

    private fun getRedactedHeaders(headers: HttpHeaders) =
        headers.mapValues {
            if (it.key.equals(HttpHeaders.AUTHORIZATION, true)) {
                "redacted"
            } else {
                it.value
            }
        }
}