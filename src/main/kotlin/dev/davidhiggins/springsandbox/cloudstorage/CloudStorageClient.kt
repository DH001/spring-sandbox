package dev.davidhiggins.springsandbox.cloudstorage

import dev.davidhiggins.springsandbox.config.baseWebClientBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.io.IOException
import java.net.URI
import java.net.URL

private const val DEFAULT_MAX_CONTENT_LENGTH = 10_000_000L

@Component
class CloudStorageClient(
    @Value("\${maxContentLength}:$DEFAULT_MAX_CONTENT_LENGTH") val maxContentLength: Long = DEFAULT_MAX_CONTENT_LENGTH
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val supportedTypes = setOf(
        MediaType.APPLICATION_PDF,
        MediaType.IMAGE_PNG,
        MediaType.IMAGE_GIF,
        MediaType.IMAGE_JPEG,
        MediaType.TEXT_PLAIN
        // Add types here that we want to support
    )

    // TODO Requires a way to request presigned URL for a bucket name from GCS

    /**
     * Web client for direct uploads to pre-signed cloud storage urls.
     *
     * No auth implemented as we use pre-signed URLs
     */
    private val uploadWebClient = baseWebClientBuilder(log).build()

    fun createAndUpload(file: CloudStorageFile, presignedUrl: String) {
        try {
            val resource = ByteArrayResource(file.bytes)
            log.info("Uploading file  to URL: {}", presignedUrl)
            val response = uploadWebClient.put()
                .uri(URI(presignedUrl)) // Must be wrapped in URI first or GCS will reject
                .body(BodyInserters.fromResource<Resource>(resource))
                .exchangeToMono { response ->
                    if (response.statusCode().is2xxSuccessful) {
                        response.bodyToMono<String>()
                    } else {
                        response.createException()
                    }
                }.block()

            if (response is WebClientResponseException) {
                log.warn(
                    "Got {} error {} with body: {}",
                    response.statusCode,
                    response.message,
                    response.responseBodyAsString
                )
                throw response
            } else {
                log.info("File uploaded to {}", presignedUrl)
            }
        } catch (e: Exception) {
            log.error("Failed to upload file to ", presignedUrl, e)
            throw e
        }
    }

    fun fetchFile(downloadUrl: URL): CloudStorageFile? {
        return try {
            val connection = downloadUrl.openConnection()
            val contentLength = connection.contentLength.toLong()
            if (contentLength <= 0) {
                throw IOException("No content for url: $downloadUrl")
            }
            if (contentLength >= maxContentLength) {
                log.warn("File exceeded max length (content-length:'$contentLength') for id: $downloadUrl")
                return null
            }

            val bytes = downloadUrl.readBytes()
            val contentType = MediaType.parseMediaType(connection.contentType)
            if (!supportedTypes.contains(contentType)) {
                throw IOException("Unsupported media-type: $contentType on URL: $downloadUrl. Supported types: $supportedTypes")
            }

            CloudStorageFile(
                bytes = bytes,
                contentLength = contentLength,
                contentType = contentType
            )
        } catch (e: Exception) {
            log.warn("Failed to download a file: {}", downloadUrl, e)
            null
        }

    }
}

data class CloudStorageFile(
    val bytes: ByteArray,
    val contentType: MediaType,
    val contentLength: Long
) {
    // Override equals() and hashCode() in data class due to presence of ByteArray
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CloudStorageFile

        if (!bytes.contentEquals(other.bytes)) return false
        if (contentType != other.contentType) return false
        if (contentLength != other.contentLength) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + contentLength.hashCode()
        return result
    }
}

