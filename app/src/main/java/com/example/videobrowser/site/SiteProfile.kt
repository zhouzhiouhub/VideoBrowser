package com.example.videobrowser.site

/**
 * 初学者阅读提示：
 * 这个文件属于“站点适配模块”。
 * 文件名 SiteProfile 可以拆开理解为“Site Profile”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：识别不同视频网站或网页宿主，并把站点专属能力交给通用浏览器流程使用。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
/**
 * 站点适配的静态描述。P8 只维护域名边界和本地脚本映射，不承接远程规则。
 */
data class SiteProfile(
    val id: String,
    val displayName: String,
    val domains: Set<String>,
    val scriptAssetPaths: List<String>,
    val cssSelectors: List<String> = emptyList(),
    val domSelectors: List<String> = emptyList()
) {
    init {
        require(id.isNotBlank()) { "Site id must not be blank." }
        require(displayName.isNotBlank()) { "Site display name must not be blank." }
        require(domains.isNotEmpty()) { "Site domains must not be empty." }
        require(domains.all { it.isNotBlank() }) { "Site domains must not contain blank values." }
        require(scriptAssetPaths.isNotEmpty()) { "Site scripts must not be empty." }
        require(scriptAssetPaths.all { it.isNotBlank() }) {
            "Site scripts must not contain blank values."
        }
    }
}
