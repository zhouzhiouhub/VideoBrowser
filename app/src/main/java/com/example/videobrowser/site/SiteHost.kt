package com.example.videobrowser.site

/**
 * 初学者阅读提示：
 * 这个文件属于“站点适配模块”。
 * 文件名 SiteHost 可以拆开理解为“Site Host”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：识别不同视频网站或网页宿主，并把站点专属能力交给通用浏览器流程使用。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
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
