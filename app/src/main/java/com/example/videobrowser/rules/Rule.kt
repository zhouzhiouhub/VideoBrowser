package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 Rule 可以拆开理解为“Rule”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.browser.ResourceType
import java.util.Locale

/**
 * 请求级规则模型。资源类型限制只使用项目可推断的安全子集。
 */
data class Rule(
    val id: String,
    val pattern: String,
    val type: RuleType,
    val action: RuleAction,
    val source: String = SOURCE_BUILT_IN,
    val domainScope: DomainScope = DomainScope.Empty,
    val thirdParty: Boolean? = null,
    val resourceTypes: Set<ResourceType> = emptySet(),
    val redirectResourceName: String? = null
) {
    init {
        require(id.isNotBlank()) { "Rule id must not be blank." }
        require(pattern.trim().isNotEmpty()) { "Rule pattern must not be blank." }
        require(action != RuleAction.NONE) { "Rule action must be ALLOW or BLOCK." }
    }

    val normalizedPattern: String = pattern.trim().lowercase(Locale.US)
    val normalizedPatternRegex: Regex? = if (type == RuleType.URL_PATTERN) {
        RequestRuleFactory.buildUrlPatternRegex(pattern)
    } else {
        null
    }

    companion object {
        const val SOURCE_BUILT_IN = "built-in"

        fun blockUrlContains(
            pattern: String,
            id: String = "block:url:$pattern",
            source: String = SOURCE_BUILT_IN
        ): Rule {
            return RequestRuleFactory.blockUrlContains(
                pattern = pattern,
                id = id,
                source = source
            )
        }

        fun blockDomainContains(
            domain: String,
            id: String = "block:domain:$domain",
            source: String = SOURCE_BUILT_IN
        ): Rule {
            return RequestRuleFactory.blockDomainContains(
                domain = domain,
                id = id,
                source = source
            )
        }

        fun allowUrlContains(
            pattern: String,
            id: String = "allow:url:$pattern",
            source: String = SOURCE_BUILT_IN
        ): Rule {
            return RequestRuleFactory.allowUrlContains(
                pattern = pattern,
                id = id,
                source = source
            )
        }

        fun allowDomainContains(
            domain: String,
            id: String = "allow:domain:$domain",
            source: String = SOURCE_BUILT_IN
        ): Rule {
            return RequestRuleFactory.allowDomainContains(
                domain = domain,
                id = id,
                source = source
            )
        }

        fun fromRequestRuleText(
            text: String,
            id: String? = null,
            source: String = SOURCE_BUILT_IN
        ): Rule? {
            return RequestRuleFactory.fromRequestRuleText(
                text = text,
                id = id,
                source = source
            )
        }
    }
}
