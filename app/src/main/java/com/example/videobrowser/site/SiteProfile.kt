package com.example.videobrowser.site

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
