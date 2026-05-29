package com.example.videobrowser.site

import java.net.URI
import java.util.Locale

/**
 * 统一处理“当前站点”的 host 识别，避免 UI、设置和拦截策略各自解析域名。
 */
object SiteHost {
    fun fromUrl(url: String?): String? {
        val value = url?.trim().orEmpty()
        if (value.isEmpty()) {
            return null
        }

        return runCatching { URI(value).host }
            .getOrNull()
            .let(::normalize)
    }

    fun normalize(host: String?): String? {
        val normalized = host
            ?.trim()
            ?.trim('.')
            ?.lowercase(Locale.ROOT)
            .orEmpty()
        return normalized.takeIf { it.isNotEmpty() }
    }
}
