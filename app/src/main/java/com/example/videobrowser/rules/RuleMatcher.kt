package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 RuleMatcher 可以拆开理解为“Rule Matcher”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.browser.ResourceType
import java.net.URI
import java.util.Locale

class RuleMatcher {
    fun matches(
        rule: Rule,
        url: String,
        host: String? = null,
        pageHost: String? = null,
        resourceType: ResourceType = ResourceType.UNKNOWN
    ): Boolean {
        val normalizedUrl = normalizeUrl(url)
        if (normalizedUrl.isEmpty()) {
            return false
        }
        if (!matchesResourceType(rule, resourceType)) {
            return false
        }
        if (!rule.domainScope.matches(pageHost)) {
            return false
        }
        if (!matchesPartyOption(rule, host ?: parseHost(url), pageHost)) {
            return false
        }

        return when (rule.type) {
            RuleType.URL_CONTAINS -> normalizedUrl.contains(rule.normalizedPattern)
            RuleType.URL_PATTERN -> rule.normalizedPatternRegex?.containsMatchIn(normalizedUrl) ?: false
            RuleType.DOMAIN_CONTAINS -> matchesDomain(
                host = normalizeHost(host ?: parseHost(url)),
                domain = normalizeHost(rule.pattern)
            )
        }
    }

    private fun matchesResourceType(rule: Rule, resourceType: ResourceType): Boolean {
        if (rule.resourceTypes.isEmpty()) {
            return true
        }
        if (resourceType == ResourceType.UNKNOWN) {
            return false
        }
        return resourceType in rule.resourceTypes
    }

    private fun matchesPartyOption(rule: Rule, requestHost: String?, pageHost: String?): Boolean {
        val expectedThirdParty = rule.thirdParty ?: return true
        val actualThirdParty = isThirdParty(requestHost, pageHost) ?: return false
        return actualThirdParty == expectedThirdParty
    }

    private fun isThirdParty(requestHost: String?, pageHost: String?): Boolean? {
        val requestSite = effectiveSite(normalizeHost(requestHost)).takeIf { it.isNotEmpty() }
            ?: return null
        val pageSite = effectiveSite(normalizeHost(pageHost)).takeIf { it.isNotEmpty() }
            ?: return null
        return requestSite != pageSite
    }

    private fun effectiveSite(host: String): String {
        val labels = host.split('.').filter { label -> label.isNotEmpty() }
        if (labels.size <= 2) {
            return host
        }
        val suffix = labels.takeLast(2)
        if (suffix[1].length == 2 && suffix[0] in COMMON_SECOND_LEVEL_SUFFIXES && labels.size >= 3) {
            return labels.takeLast(3).joinToString(".")
        }
        return suffix.joinToString(".")
    }

    private fun matchesDomain(host: String, domain: String): Boolean {
        if (host.isEmpty() || domain.isEmpty()) {
            return false
        }

        return host == domain || host.endsWith(".$domain")
    }

    private fun normalizeUrl(url: String): String {
        return url.trim().lowercase(Locale.US)
    }

    private fun normalizeHost(host: String?): String {
        return host.orEmpty()
            .trim()
            .trim('.')
            .lowercase(Locale.US)
    }

    private fun parseHost(url: String): String? {
        return runCatching { URI(url.trim()).host }.getOrNull()
    }

    private companion object {
        private val COMMON_SECOND_LEVEL_SUFFIXES = setOf(
            "ac",
            "co",
            "com",
            "edu",
            "gov",
            "net",
            "org"
        )
    }
}
