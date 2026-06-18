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
        buildUrlPatternRegex(pattern)
    } else {
        null
    }

    companion object {
        const val SOURCE_BUILT_IN = "built-in"

        /**
         * 函数 `blockUrlContains`：封装 `block Url Contains` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param pattern 参数类型为 `String`，表示函数执行 `pattern` 相关逻辑时需要读取或处理的输入。
         * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
         * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun blockUrlContains(
            pattern: String,
            id: String = "block:url:$pattern",
            source: String = SOURCE_BUILT_IN
        ): Rule {
            return requestRule(
                id = id,
                pattern = pattern,
                type = RuleType.URL_CONTAINS,
                action = RuleAction.BLOCK,
                source = source
            )
        }

        /**
         * 函数 `blockDomainContains`：封装 `block Domain Contains` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param domain 参数类型为 `String`，表示函数执行 `domain` 相关逻辑时需要读取或处理的输入。
         * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
         * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun blockDomainContains(
            domain: String,
            id: String = "block:domain:$domain",
            source: String = SOURCE_BUILT_IN
        ): Rule {
            return requestRule(
                id = id,
                pattern = domain,
                type = RuleType.DOMAIN_CONTAINS,
                action = RuleAction.BLOCK,
                source = source
            )
        }

        /**
         * 函数 `allowUrlContains`：封装 `allow Url Contains` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param pattern 参数类型为 `String`，表示函数执行 `pattern` 相关逻辑时需要读取或处理的输入。
         * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
         * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun allowUrlContains(
            pattern: String,
            id: String = "allow:url:$pattern",
            source: String = SOURCE_BUILT_IN
        ): Rule {
            return requestRule(
                id = id,
                pattern = pattern,
                type = RuleType.URL_CONTAINS,
                action = RuleAction.ALLOW,
                source = source
            )
        }

        /**
         * 函数 `allowDomainContains`：封装 `allow Domain Contains` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param domain 参数类型为 `String`，表示函数执行 `domain` 相关逻辑时需要读取或处理的输入。
         * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
         * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun allowDomainContains(
            domain: String,
            id: String = "allow:domain:$domain",
            source: String = SOURCE_BUILT_IN
        ): Rule {
            return requestRule(
                id = id,
                pattern = domain,
                type = RuleType.DOMAIN_CONTAINS,
                action = RuleAction.ALLOW,
                source = source
            )
        }

        /**
         * 函数 `fromRequestRuleText`：封装 `from Request Rule Text` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
         * @param id 参数类型为 `String?`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
         * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun fromRequestRuleText(
            text: String,
            id: String? = null,
            source: String = SOURCE_BUILT_IN
        ): Rule? {
            val trimmed = text.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("!") || trimmed.startsWith("#")) {
                return null
            }

            val action = if (trimmed.startsWith("@@")) {
                RuleAction.ALLOW
            } else {
                RuleAction.BLOCK
            }
            val rawBody = if (action == RuleAction.ALLOW) {
                trimmed.removePrefix("@@").trim()
            } else {
                trimmed
            }
            if (rawBody.isBlank()) {
                return null
            }

            val (body, optionsText) = splitRequestOptions(rawBody)
            if (body.isBlank() || hasUnsupportedRequestSyntax(body)) {
                return null
            }
            val options = parseRequestOptions(optionsText) ?: return null

            val type = requestRuleTypeFor(body)
            val domainPattern = parsePureDomainRule(body)
            val pattern = domainPattern ?: body
            if (action == RuleAction.ALLOW && options.redirectResourceName != null) {
                return null
            }
            val generatedId = id ?: "${action.name.lowercase(Locale.US)}:" +
                "${type.name.lowercase(Locale.US)}:$pattern"
            return requestRule(
                id = generatedId,
                pattern = pattern,
                type = type,
                action = action,
                source = source,
                domainScope = options.domainScope,
                thirdParty = options.thirdParty,
                resourceTypes = options.resourceTypes,
                redirectResourceName = options.redirectResourceName
            )
        }

        /**
         * 函数 `requestRule`：处理 `request Rule` 对应的事件或请求，集中完成校验、状态更新和回调通知。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param id 参数类型为 `String`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
         * @param pattern 参数类型为 `String`，表示函数执行 `pattern` 相关逻辑时需要读取或处理的输入。
         * @param type 参数类型为 `RuleType`，表示函数执行 `type` 相关逻辑时需要读取或处理的输入。
         * @param action 参数类型为 `RuleAction`，表示函数执行 `action` 相关逻辑时需要读取或处理的输入。
         * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
         * @param domainScope 参数类型为 `DomainScope`，表示函数执行 `domainScope` 相关逻辑时需要读取或处理的输入。
         * @param thirdParty 参数类型为 `Boolean?`，表示函数执行 `thirdParty` 相关逻辑时需要读取或处理的输入。
         * @param resourceTypes 参数类型为 `Set<ResourceType>`，表示函数执行 `resourceTypes` 相关逻辑时需要读取或处理的输入。
         * @param redirectResourceName 参数类型为 `String?`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun requestRule(
            id: String,
            pattern: String,
            type: RuleType,
            action: RuleAction,
            source: String,
            domainScope: DomainScope = DomainScope.Empty,
            thirdParty: Boolean? = null,
            resourceTypes: Set<ResourceType> = emptySet(),
            redirectResourceName: String? = null
        ): Rule {
            val normalizedPattern = when (type) {
                RuleType.URL_CONTAINS -> pattern.trim()
                RuleType.URL_PATTERN -> pattern.trim()
                RuleType.DOMAIN_CONTAINS -> pattern.trim().trim('.')
            }
            return Rule(
                id = id,
                pattern = normalizedPattern,
                type = type,
                action = action,
                source = source,
                domainScope = domainScope,
                thirdParty = thirdParty,
                resourceTypes = resourceTypes,
                redirectResourceName = redirectResourceName
            )
        }

        /**
         * 函数 `requestRuleTypeFor`：处理 `request Rule Type For` 对应的事件或请求，集中完成校验、状态更新和回调通知。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun requestRuleTypeFor(text: String): RuleType {
            parsePureDomainRule(text)?.let {
                return RuleType.DOMAIN_CONTAINS
            }
            return if (hasPatternSyntax(text)) {
                RuleType.URL_PATTERN
            } else {
                RuleType.URL_CONTAINS
            }
        }

        /**
         * 函数 `parsePureDomainRule`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun parsePureDomainRule(text: String): String? {
            if (!text.startsWith("||") || !text.endsWith("^")) {
                return null
            }

            val domain = text
                .removePrefix("||")
                .removeSuffix("^")
                .trim()
                .trim('.')
            if (domain.isBlank()) {
                return null
            }

            val hasOnlyDomainCharacters = domain.all { char ->
                char.isLetterOrDigit() || char == '-' || char == '.'
            }
            return domain.takeIf { hasOnlyDomainCharacters }
        }

        /**
         * 函数 `hasUnsupportedRequestSyntax`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun hasUnsupportedRequestSyntax(text: String): Boolean {
            return text.contains("##") ||
                text.contains("#@#") ||
                text.contains("#%#")
        }

        /**
         * 函数 `hasPatternSyntax`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun hasPatternSyntax(text: String): Boolean {
            return text.contains('*') ||
                text.contains('^') ||
                text.startsWith("|") ||
                text.endsWith("|")
        }

        /**
         * 函数 `splitRequestOptions`：封装 `split Request Options` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun splitRequestOptions(text: String): Pair<String, String?> {
            val optionsIndex = text.indexOf('$')
            if (optionsIndex < 0) {
                return text.trim() to null
            }
            return text.substring(0, optionsIndex).trim() to
                text.substring(optionsIndex + 1).trim().takeIf { it.isNotEmpty() }
        }

        /**
         * 函数 `parseRequestOptions`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param text 参数类型为 `String?`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun parseRequestOptions(text: String?): RequestRuleOptions? {
            if (text.isNullOrBlank()) {
                return RequestRuleOptions()
            }

            var domainScope = DomainScope.Empty
            var thirdParty: Boolean? = null
            val resourceTypes = mutableSetOf<ResourceType>()
            var redirectResourceName: String? = null
            text.split(',')
                .map { option -> option.trim() }
                .filter { option -> option.isNotEmpty() }
                .forEach { option ->
                    val lower = option.lowercase(Locale.US)
                    when {
                        lower == "third-party" || lower == "3p" -> thirdParty = true
                        lower == "~third-party" || lower == "1p" || lower == "first-party" -> {
                            thirdParty = false
                        }
                        lower.startsWith("domain=") -> {
                            domainScope = parseOptionDomainScope(option.substringAfter("="))
                                ?: return null
                        }
                        lower.startsWith("redirect=") -> {
                            redirectResourceName = parseRedirectResourceName(option.substringAfter("="))
                                ?: return null
                        }
                        RESOURCE_TYPE_OPTIONS.containsKey(lower) -> {
                            resourceTypes.addAll(RESOURCE_TYPE_OPTIONS.getValue(lower))
                        }
                        lower.startsWith("~") &&
                            RESOURCE_TYPE_OPTIONS.containsKey(lower.removePrefix("~")) -> {
                            return null
                        }
                        else -> return null
                    }
                }
            return RequestRuleOptions(
                domainScope = domainScope,
                thirdParty = thirdParty,
                resourceTypes = resourceTypes.toSet(),
                redirectResourceName = redirectResourceName
            )
        }

        /**
         * 函数 `parseRedirectResourceName`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun parseRedirectResourceName(text: String): String? {
            val normalized = text.trim().lowercase(Locale.US)
            return normalized.takeIf { resourceName ->
                resourceName in SUPPORTED_REDIRECT_RESOURCES
            }
        }

        /**
         * 函数 `parseOptionDomainScope`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun parseOptionDomainScope(text: String): DomainScope? {
            val included = mutableSetOf<String>()
            val excluded = mutableSetOf<String>()
            text.split('|')
                .map { domain -> domain.trim() }
                .filter { domain -> domain.isNotEmpty() }
                .forEach { rawDomain ->
                    val isExcluded = rawDomain.startsWith("~")
                    val normalized = rawDomain
                        .removePrefix("~")
                        .trim()
                        .trim('.')
                        .lowercase(Locale.US)
                    if (!isValidDomainPattern(normalized)) {
                        return null
                    }
                    if (isExcluded) {
                        excluded += normalized
                    } else {
                        included += normalized
                    }
                }
            return DomainScope(
                includedDomains = included,
                excludedDomains = excluded
            )
        }

        /**
         * 函数 `isValidDomainPattern`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param domain 参数类型为 `String`，表示函数执行 `domain` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun isValidDomainPattern(domain: String): Boolean {
            return domain.isNotBlank() &&
                domain.all { char -> char.isLetterOrDigit() || char == '-' || char == '.' }
        }

        /**
         * 函数 `buildUrlPatternRegex`：创建 `build Url Pattern Regex` 需要的对象、视图或配置，并返回给后续流程使用。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param pattern 参数类型为 `String`，表示函数执行 `pattern` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun buildUrlPatternRegex(pattern: String): Regex? {
            val source = pattern.trim().lowercase(Locale.US)
            if (source.isEmpty()) {
                return null
            }

            val builder = StringBuilder()
            var index = 0
            if (source.startsWith("||")) {
                builder.append("^[a-z][a-z0-9+.-]*://(?:[^/?#]+\\.)?")
                index = 2
            } else if (source.startsWith("|")) {
                builder.append("^")
                index = 1
            }

            val endAnchored = source.endsWith("|") && source.length > index
            val end = if (endAnchored) source.length - 1 else source.length
            while (index < end) {
                when (val char = source[index]) {
                    '*' -> builder.append(".*")
                    '^' -> builder.append("(?:[^A-Za-z0-9_\\-.%]|$)")
                    else -> builder.append(Regex.escape(char.toString()))
                }
                index += 1
            }
            if (endAnchored) {
                builder.append("$")
            }

            return runCatching { Regex(builder.toString(), RegexOption.IGNORE_CASE) }.getOrNull()
        }

        private val RESOURCE_TYPE_OPTIONS = mapOf(
            "document" to setOf(ResourceType.DOCUMENT),
            "script" to setOf(ResourceType.SCRIPT),
            "image" to setOf(ResourceType.IMAGE),
            "stylesheet" to setOf(ResourceType.STYLESHEET),
            "css" to setOf(ResourceType.STYLESHEET),
            "media" to setOf(ResourceType.MEDIA),
            "font" to setOf(ResourceType.FONT),
            "object" to setOf(ResourceType.OTHER),
            "subdocument" to setOf(ResourceType.DOCUMENT),
            "frame" to setOf(ResourceType.DOCUMENT),
            "xmlhttprequest" to setOf(ResourceType.XHR, ResourceType.FETCH),
            "xhr" to setOf(ResourceType.XHR, ResourceType.FETCH),
            "fetch" to setOf(ResourceType.FETCH),
            "ping" to setOf(ResourceType.FETCH),
            "beacon" to setOf(ResourceType.FETCH),
            "other" to setOf(ResourceType.OTHER)
        )

        private val SUPPORTED_REDIRECT_RESOURCES = setOf(
            "noopjs",
            "noopcss",
            "nooptext"
        )
    }
}

private data class RequestRuleOptions(
    val domainScope: DomainScope = DomainScope.Empty,
    val thirdParty: Boolean? = null,
    val resourceTypes: Set<ResourceType> = emptySet(),
    val redirectResourceName: String? = null
)
