package com.example.videobrowser.adblock

/**
 * 初学者阅读提示：
 * 这个文件属于“广告请求拦截模块”。
 * 文件名 SyntheticResponseRegistry 可以拆开理解为“Synthetic Response Registry”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：把 WebView 的网络请求交给规则系统判断，并在需要拦截时返回安全的本地响应。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import java.util.Locale

class SyntheticResponseRegistry(
    private val specs: Map<String, SyntheticResponseSpec> = defaultSpecs()
) {
    fun get(name: String?): SyntheticResponseSpec? {
        val normalizedName = normalizeName(name) ?: return null
        return specs[normalizedName]
    }

    fun contains(name: String?): Boolean {
        return get(name) != null
    }

    private fun normalizeName(name: String?): String? {
        val value = name
            ?.trim()
            ?.lowercase(Locale.US)
            ?: return null
        if (value.isEmpty() || value.contains("://") || value.any { char -> char == '/' || char == '\\' }) {
            return null
        }
        return value
    }

    companion object {
        const val NOOP_JS = "noopjs"
        const val NOOP_CSS = "noopcss"
        const val NOOP_TEXT = "nooptext"

        private fun defaultSpecs(): Map<String, SyntheticResponseSpec> {
            return listOf(
                SyntheticResponseSpec(
                    name = NOOP_JS,
                    mimeType = "application/javascript",
                    body = "/* noop */\n".toByteArray(Charsets.UTF_8)
                ),
                SyntheticResponseSpec(
                    name = NOOP_CSS,
                    mimeType = "text/css",
                    body = "/* noop */\n".toByteArray(Charsets.UTF_8)
                ),
                SyntheticResponseSpec(
                    name = NOOP_TEXT,
                    mimeType = "text/plain",
                    body = ByteArray(0)
                )
            ).associateBy { spec -> spec.name }
        }
    }
}
