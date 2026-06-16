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
    /**
     * 函数 `get`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String?`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun get(name: String?): SyntheticResponseSpec? {
        val normalizedName = normalizeName(name) ?: return null
        return specs[normalizedName]
    }

    /**
     * 函数 `contains`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String?`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun contains(name: String?): Boolean {
        return get(name) != null
    }

    /**
     * 函数 `normalizeName`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String?`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

        /**
         * 函数 `defaultSpecs`：封装 `default Specs` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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
