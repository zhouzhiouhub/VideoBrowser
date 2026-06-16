package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 ResourceTypeResolver 可以拆开理解为“Resource Type Resolver”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import java.net.URI
import java.util.Locale

/**
 * 通过 WebView 暴露的有限字段保守推断资源类型；无法确认时返回 UNKNOWN，避免误杀。
 */
object ResourceTypeResolver {
    fun resolve(
        requestUrl: String,
        isForMainFrame: Boolean,
        requestHeaders: Map<String, String> = emptyMap()
    ): ResourceType {
        if (isForMainFrame) {
            return ResourceType.DOCUMENT
        }

        val headers = CaseInsensitiveHeaders(requestHeaders)
        typeFromFetchDest(headers["sec-fetch-dest"])?.let { return it }
        typeFromRequestedWith(headers["x-requested-with"])?.let { return it }
        typeFromAccept(headers["accept"], headers["sec-fetch-dest"])?.let { return it }
        typeFromUrlExtension(requestUrl)?.let { return it }
        return ResourceType.UNKNOWN
    }

    private fun typeFromFetchDest(value: String?): ResourceType? {
        return when (value?.trim()?.lowercase(Locale.US)) {
            "document", "iframe", "frame" -> ResourceType.DOCUMENT
            "script", "worker", "sharedworker", "serviceworker" -> ResourceType.SCRIPT
            "style" -> ResourceType.STYLESHEET
            "image" -> ResourceType.IMAGE
            "font" -> ResourceType.FONT
            "audio", "video", "track" -> ResourceType.MEDIA
            "object", "embed", "manifest", "report", "xslt" -> ResourceType.OTHER
            "empty" -> null
            else -> null
        }
    }

    private fun typeFromRequestedWith(value: String?): ResourceType? {
        return if (value.equals("XMLHttpRequest", ignoreCase = true)) {
            ResourceType.XHR
        } else {
            null
        }
    }

    private fun typeFromAccept(value: String?, fetchDest: String?): ResourceType? {
        val accept = value?.lowercase(Locale.US).orEmpty()
        if (accept.isBlank()) {
            return null
        }
        return when {
            accept.contains("text/css") -> ResourceType.STYLESHEET
            accept.contains("javascript") || accept.contains("ecmascript") -> ResourceType.SCRIPT
            accept.contains("image/") -> ResourceType.IMAGE
            accept.contains("video/") || accept.contains("audio/") -> ResourceType.MEDIA
            accept.contains("font/") ||
                accept.contains("application/font") ||
                accept.contains("application/vnd.ms-fontobject") -> ResourceType.FONT
            fetchDest.equals("empty", ignoreCase = true) &&
                (accept.contains("application/json") || accept.contains("text/plain")) -> {
                ResourceType.FETCH
            }
            else -> null
        }
    }

    private fun typeFromUrlExtension(url: String): ResourceType? {
        val path = pathFor(url).lowercase(Locale.US)
        val extension = path.substringAfterLast('.', missingDelimiterValue = "")
        if (extension.isBlank() || extension.contains('/')) {
            return null
        }
        return when (extension) {
            "js", "mjs" -> ResourceType.SCRIPT
            "css" -> ResourceType.STYLESHEET
            "png", "jpg", "jpeg", "gif", "webp", "svg", "ico", "bmp", "avif" -> {
                ResourceType.IMAGE
            }
            "mp4", "webm", "m3u8", "mpd", "mov", "m4v", "mp3", "aac", "ogg", "wav", "flac" -> {
                ResourceType.MEDIA
            }
            "woff", "woff2", "ttf", "otf", "eot" -> ResourceType.FONT
            "json" -> ResourceType.FETCH
            else -> null
        }
    }

    private fun pathFor(url: String): String {
        return runCatching { URI(url.trim()).path.orEmpty() }.getOrElse {
            url.substringBefore('?').substringBefore('#')
        }
    }

    private class CaseInsensitiveHeaders(headers: Map<String, String>) {
        private val values = headers.entries.associate { (key, value) ->
            key.lowercase(Locale.US) to value
        }

        operator fun get(name: String): String? {
            return values[name.lowercase(Locale.US)]
        }
    }
}
