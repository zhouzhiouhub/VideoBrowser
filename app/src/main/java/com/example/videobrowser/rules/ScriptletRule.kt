package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 ScriptletRule 可以拆开理解为“Scriptlet Rule”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.site.SiteHost

data class ScriptletRule(
    val id: String,
    val name: String,
    val arguments: List<String> = emptyList(),
    val source: String = Rule.SOURCE_BUILT_IN,
    val domainScope: DomainScope = DomainScope.Empty
) {
    init {
        require(id.isNotBlank()) { "Scriptlet rule id must not be blank." }
        require(name.isNotBlank()) { "Scriptlet name must not be blank." }
    }

    fun matchesPage(pageUrl: String?): Boolean {
        return domainScope.matches(SiteHost.fromUrl(pageUrl))
    }
}
