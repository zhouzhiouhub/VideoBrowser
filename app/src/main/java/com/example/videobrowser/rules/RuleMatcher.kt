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
    /**
     * 函数 `matches`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rule 参数类型为 `Rule`，表示函数执行 `rule` 相关逻辑时需要读取或处理的输入。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     * @param resourceType 参数类型为 `ResourceType`，表示函数执行 `resourceType` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `matchesResourceType`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rule 参数类型为 `Rule`，表示函数执行 `rule` 相关逻辑时需要读取或处理的输入。
     * @param resourceType 参数类型为 `ResourceType`，表示函数执行 `resourceType` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun matchesResourceType(rule: Rule, resourceType: ResourceType): Boolean {
        if (rule.resourceTypes.isEmpty()) {
            return true
        }
        if (resourceType == ResourceType.UNKNOWN) {
            return false
        }
        return resourceType in rule.resourceTypes
    }

    /**
     * 函数 `matchesPartyOption`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rule 参数类型为 `Rule`，表示函数执行 `rule` 相关逻辑时需要读取或处理的输入。
     * @param requestHost 参数类型为 `String?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun matchesPartyOption(rule: Rule, requestHost: String?, pageHost: String?): Boolean {
        val expectedThirdParty = rule.thirdParty ?: return true
        val actualThirdParty = isThirdParty(requestHost, pageHost) ?: return false
        return actualThirdParty == expectedThirdParty
    }

    /**
     * 函数 `isThirdParty`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param requestHost 参数类型为 `String?`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isThirdParty(requestHost: String?, pageHost: String?): Boolean? {
        val requestSite = effectiveSite(normalizeHost(requestHost)).takeIf { it.isNotEmpty() }
            ?: return null
        val pageSite = effectiveSite(normalizeHost(pageHost)).takeIf { it.isNotEmpty() }
            ?: return null
        return requestSite != pageSite
    }

    /**
     * 函数 `effectiveSite`：封装 `effective Site` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `matchesDomain`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param domain 参数类型为 `String`，表示函数执行 `domain` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun matchesDomain(host: String, domain: String): Boolean {
        if (host.isEmpty() || domain.isEmpty()) {
            return false
        }

        return host == domain || host.endsWith(".$domain")
    }

    /**
     * 函数 `normalizeUrl`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeUrl(url: String): String {
        return url.trim().lowercase(Locale.US)
    }

    /**
     * 函数 `normalizeHost`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeHost(host: String?): String {
        return host.orEmpty()
            .trim()
            .trim('.')
            .lowercase(Locale.US)
    }

    /**
     * 函数 `parseHost`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
