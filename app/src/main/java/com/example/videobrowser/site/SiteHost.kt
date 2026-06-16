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
    /**
     * 函数 `fromUrl`：封装 `from Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun fromUrl(url: String?): String? {
        val value = url?.trim().orEmpty()
        if (value.isEmpty()) {
            return null
        }

        return runCatching { URI(value).host }
            .getOrNull()
            .let(::normalize)
    }

    /**
     * 函数 `normalize`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun normalize(host: String?): String? {
        val normalized = host
            ?.trim()
            ?.trim('.')
            ?.lowercase(Locale.ROOT)
            .orEmpty()
        return normalized.takeIf { it.isNotEmpty() }
    }
}
