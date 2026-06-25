package com.example.videobrowser.browser.search

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fetches small HTML/XML documents for search engine template probing.
 *
 * The caller supplies the byte limit per request; this class only handles
 * HTTP setup, redirects that remain HTTP(S), and bounded UTF-8 reads.
 */
class SearchEngineProbeFetcher(
    private val networkTimeoutMs: Int = DEFAULT_NETWORK_TIMEOUT_MS,
    private val openConnection: (URL) -> HttpURLConnection = { url ->
        url.openConnection() as HttpURLConnection
    }
) {
    init {
        require(networkTimeoutMs > 0) { "Network timeout must be positive." }
    }

    fun fetchText(url: String, maxBytes: Int): String? {
        if (maxBytes <= 0) {
            return null
        }
        val normalizedUrl = SearchEngineTemplateProber.normalizeProbeUrl(url) ?: return null
        val connection = runCatching {
            openConnection(URL(normalizedUrl)).apply {
                connectTimeout = networkTimeoutMs
                readTimeout = networkTimeoutMs
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml,text/xml")
                setRequestProperty("User-Agent", USER_AGENT)
            }
        }.getOrNull() ?: return null

        return try {
            val statusCode = connection.responseCode
            if (statusCode !in 200..299) {
                return null
            }
            if (SearchEngineTemplateProber.normalizeProbeUrl(connection.url.toString()) == null) {
                return null
            }
            val contentLength = connection.contentLengthLong
            if (contentLength > maxBytes) {
                return null
            }
            connection.inputStream.use { input -> readTextWithByteLimit(input, maxBytes) }
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun readTextWithByteLimit(input: InputStream, maxBytes: Int): String? {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_SIZE)
        var totalBytes = 0
        while (true) {
            val read = input.read(buffer)
            if (read == -1) {
                break
            }
            if (totalBytes + read > maxBytes) {
                return null
            }
            output.write(buffer, 0, read)
            totalBytes += read
        }
        return output.toByteArray().toString(Charsets.UTF_8)
    }

    companion object {
        const val DEFAULT_NETWORK_TIMEOUT_MS = 5_000
        private const val BUFFER_SIZE = 8 * 1024
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android) VideoBrowser"
    }
}
