package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 DomainScope 可以拆开理解为“Domain Scope”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.site.SiteHost

data class DomainScope(
    val includedDomains: Set<String> = emptySet(),
    val excludedDomains: Set<String> = emptySet()
) {
    val normalizedIncludedDomains: Set<String> = includedDomains.mapNotNull(SiteHost::normalize).toSet()
    val normalizedExcludedDomains: Set<String> = excludedDomains.mapNotNull(SiteHost::normalize).toSet()

    val hasRestrictions: Boolean
        get() = normalizedIncludedDomains.isNotEmpty() || normalizedExcludedDomains.isNotEmpty()

    /**
     * 函数 `matches`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun matches(host: String?): Boolean {
        if (!hasRestrictions) {
            return true
        }

        val normalizedHost = SiteHost.normalize(host) ?: return false
        if (normalizedExcludedDomains.any { domain -> hostMatchesDomain(normalizedHost, domain) }) {
            return false
        }
        if (normalizedIncludedDomains.isEmpty()) {
            return true
        }
        return normalizedIncludedDomains.any { domain -> hostMatchesDomain(normalizedHost, domain) }
    }

    companion object {
        val Empty = DomainScope()

        /**
         * 函数 `hostMatchesDomain`：封装 `host Matches Domain` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param host 参数类型为 `String`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
         * @param domain 参数类型为 `String`，表示函数执行 `domain` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun hostMatchesDomain(host: String, domain: String): Boolean {
            return host == domain || host.endsWith(".$domain")
        }
    }
}
