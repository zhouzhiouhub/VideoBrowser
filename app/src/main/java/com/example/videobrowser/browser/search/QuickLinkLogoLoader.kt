package com.example.videobrowser.browser.search

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

internal object QuickLinkLogoLoader {
    private val executor = Executors.newFixedThreadPool(2)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val bitmapCache = ConcurrentHashMap<String, Bitmap>()
    private val missingLogoKeys = ConcurrentHashMap.newKeySet<String>()

    fun load(pageUrl: String, onLogoLoaded: (Bitmap?) -> Unit) {
        val cacheKey = QuickLinkLogoResolver.cacheKey(pageUrl) ?: return
        bitmapCache[cacheKey]?.let { bitmap ->
            mainHandler.post { onLogoLoaded(bitmap) }
            return
        }
        if (cacheKey in missingLogoKeys) {
            return
        }

        executor.execute {
            val bitmap = loadBitmap(pageUrl)
            if (bitmap != null) {
                bitmapCache[cacheKey] = bitmap
            } else {
                missingLogoKeys.add(cacheKey)
            }
            mainHandler.post { onLogoLoaded(bitmap) }
        }
    }

    private fun loadBitmap(pageUrl: String): Bitmap? {
        val directBitmap = QuickLinkLogoResolver.logoUrlsFor(pageUrl)
            .firstNotNullOfOrNull(::fetchBitmap)
        if (directBitmap != null) {
            return directBitmap
        }

        return fetchHtmlLogoUrls(pageUrl).firstNotNullOfOrNull(::fetchBitmap)
    }

    private fun fetchHtmlLogoUrls(pageUrl: String): List<String> {
        val connection = openConnection(pageUrl) ?: return emptyList()
        return try {
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml")
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return emptyList()
            }
            val html = connection.inputStream.use { input -> input.readLimitedUtf8(MAX_HTML_BYTES) }
            QuickLinkLogoResolver.logoUrlsFromHtml(pageUrl, html)
        } catch (_: Exception) {
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchBitmap(url: String): Bitmap? {
        val connection = openConnection(url) ?: return null
        return try {
            connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return null
            }
            connection.inputStream.use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(url: String): HttpURLConnection? {
        return runCatching {
            (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                instanceFollowRedirects = true
                useCaches = true
                setRequestProperty("User-Agent", USER_AGENT)
            }
        }.getOrNull()
    }

    private fun InputStream.readLimitedUtf8(maxBytes: Int): String {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val output = ByteArrayOutputStream()
        var remaining = maxBytes
        while (remaining > 0) {
            val read = read(buffer, 0, minOf(buffer.size, remaining))
            if (read <= 0) {
                break
            }
            output.write(buffer, 0, read)
            remaining -= read
        }
        return output.toString(Charsets.UTF_8.name())
    }

    private const val CONNECT_TIMEOUT_MS = 2500
    private const val READ_TIMEOUT_MS = 3500
    private const val MAX_HTML_BYTES = 96 * 1024
    private const val DEFAULT_BUFFER_SIZE = 4096
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android) VideoBrowser"
}
