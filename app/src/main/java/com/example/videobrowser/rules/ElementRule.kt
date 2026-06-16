package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 ElementRule 可以拆开理解为“Element Rule”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.site.SiteHost

data class ElementRule(
    val id: String,
    val selector: String,
    val type: ElementRuleType,
    val source: String = Rule.SOURCE_BUILT_IN,
    val domains: Set<String> = emptySet(),
    val excludedDomains: Set<String> = emptySet()
) {
    init {
        require(id.isNotBlank()) { "Element rule id must not be blank." }
        require(selector.trim().isNotEmpty()) { "Element selector must not be blank." }
    }

    val normalizedDomains: Set<String> = domains.mapNotNull(SiteHost::normalize).toSet()
    val normalizedExcludedDomains: Set<String> = excludedDomains.mapNotNull(SiteHost::normalize).toSet()

    /**
     * 函数 `matchesPage`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun matchesPage(pageUrl: String?): Boolean {
        return DomainScope(
            includedDomains = normalizedDomains,
            excludedDomains = normalizedExcludedDomains
        ).matches(SiteHost.fromUrl(pageUrl))
    }
}

enum class ElementRuleType {
    CSS_HIDE,
    CSS_UNHIDE,
    DOM_REMOVE
}
