package com.example.videobrowser.site

/**
 * 初学者阅读提示：
 * 这个文件属于“站点适配模块”。
 * 文件名 SiteAdapterRegistry 可以拆开理解为“Site Adapter Registry”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：识别不同视频网站或网页宿主，并把站点专属能力交给通用浏览器流程使用。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import com.example.videobrowser.utils.HostNameNormalizer

/**
 * 站点适配器注册表。
 *
 * 通用脚本先处理大多数网页；当 URL 命中特定站点时，这里会返回额外的站点脚本。
 * 例如某些视频网站有自定义播放器 API，需要专门脚本把播放、倍速、进度控制接到统一协议。
 */
class SiteAdapterRegistry(
    private val adapters: List<SiteAdapter>
) {
    /**
     * 函数 `matchingAdapters`：封装 `matching Adapters` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun matchingAdapters(url: String?): List<SiteAdapter> {
        val normalizedUrl = url?.trim().orEmpty()
        if (normalizedUrl.isBlank()) {
            return emptyList()
        }
        return adapters.filter { adapter -> adapter.matches(normalizedUrl) }
    }

    /**
     * 函数 `scriptFilesFor`：封装 `script Files For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun scriptFilesFor(url: String?): List<String> {
        return matchingAdapters(url)
            .flatMap { adapter -> adapter.scriptFiles() }
            .distinct()
    }

    companion object {
        /**
         * 函数 `default`：封装 `default` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun default(): SiteAdapterRegistry {
            return SiteAdapterRegistry(
                listOf(
                    domainAdapter(
                        id = "youtube",
                        displayName = "YouTube",
                        domains = setOf("youtube.com"),
                        scriptAssetPaths = listOf("scripts/youtube.js")
                    ),
                    domainAdapter(
                        id = "bilibili",
                        displayName = "Bilibili",
                        domains = setOf("bilibili.com"),
                        scriptAssetPaths = listOf(
                            "scripts/bilibili_overlay_cleanup.js",
                            "scripts/bilibili_browser_choice_cleanup.js",
                            "scripts/bilibili_player_api.js",
                            "scripts/bilibili.js"
                        )
                    ),
                    domainAdapter(
                        id = "iqiyi",
                        displayName = "iQIYI",
                        domains = setOf("iqiyi.com"),
                        scriptAssetPaths = listOf("scripts/iqiyi.js")
                    ),
                    domainAdapter(
                        id = "tencent",
                        displayName = "Tencent Video",
                        domains = setOf("v.qq.com"),
                        scriptAssetPaths = listOf("scripts/tencent.js")
                    ),
                    domainAdapter(
                        id = "youku",
                        displayName = "Youku",
                        domains = setOf("youku.com"),
                        scriptAssetPaths = listOf("scripts/youku.js")
                    )
                )
            )
        }

        /**
         * 函数 `domainAdapter`：封装 `domain Adapter` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
         * @param displayName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param domains 参数类型为 `Set<String>`，表示函数执行 `domains` 相关逻辑时需要读取或处理的输入。
         * @param scriptAssetPaths 参数类型为 `List<String>`，表示函数执行 `scriptAssetPaths` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun domainAdapter(
            id: String,
            displayName: String,
            domains: Set<String>,
            scriptAssetPaths: List<String>
        ): SiteAdapter {
            return DomainSiteAdapter(
                SiteProfile(
                    id = id,
                    displayName = displayName,
                    domains = domains,
                    scriptAssetPaths = scriptAssetPaths
                )
            )
        }
    }
}

private class DomainSiteAdapter(
    override val profile: SiteProfile
) : SiteAdapter {
    private val domains = profile.domains.mapNotNull(HostNameNormalizer::normalize)

    /**
     * 函数 `matches`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun matches(url: String): Boolean {
        val host = HostNameNormalizer.fromUrlOrBareHost(url) ?: return false
        return domains.any { domain -> HostNameNormalizer.matchesDomainOrSubdomain(host, domain) }
    }
}
