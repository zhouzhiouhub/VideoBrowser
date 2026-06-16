package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 RemoveParamRule 可以拆开理解为“Remove Param Rule”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import java.util.Locale

data class RemoveParamRule(
    val id: String,
    val pattern: String,
    val type: RuleType,
    val parameterName: String,
    val source: String = Rule.SOURCE_BUILT_IN,
    val domainScope: DomainScope = DomainScope.Empty
) {
    init {
        require(id.isNotBlank()) { "RemoveParam rule id must not be blank." }
        require(pattern.trim().isNotEmpty()) { "RemoveParam pattern must not be blank." }
        require(parameterName.trim().isNotEmpty()) { "RemoveParam parameter name must not be blank." }
    }

    fun toRequestMatcherRule(): Rule {
        return Rule(
            id = "$id:matcher",
            pattern = pattern,
            type = type,
            action = RuleAction.BLOCK,
            source = source,
            domainScope = domainScope
        )
    }

    companion object {
        fun fromAdGuardRuleText(
            text: String,
            id: String? = null,
            source: String = Rule.SOURCE_BUILT_IN
        ): RemoveParamRule? {
            val trimmed = text.trim()
            if (trimmed.isEmpty() ||
                trimmed.startsWith("!") ||
                trimmed.startsWith("#") ||
                trimmed.startsWith("@@")
            ) {
                return null
            }

            val (body, optionsText) = splitOptions(trimmed)
            if (body.isBlank() || optionsText.isNullOrBlank()) {
                return null
            }

            var parameterName: String? = null
            var domainOption: String? = null
            optionsText.split(',')
                .map { option -> option.trim() }
                .filter { option -> option.isNotEmpty() }
                .forEach { option ->
                    val lower = option.lowercase(Locale.US)
                    when {
                        lower.startsWith("removeparam=") -> {
                            parameterName = parseParameterName(option.substringAfter("="))
                                ?: return null
                        }
                        lower.startsWith("domain=") -> {
                            domainOption = option.substringAfter("=")
                        }
                        else -> return null
                    }
                }

            val parameter = parameterName ?: return null
            val matcherText = buildString {
                append(body)
                domainOption?.let { option ->
                    append('$')
                    append("domain=")
                    append(option)
                }
            }
            val matcherRule = Rule.fromRequestRuleText(
                text = matcherText,
                id = "${id ?: "removeparam"}:request",
                source = source
            ) ?: return null

            return RemoveParamRule(
                id = id ?: "removeparam:${matcherRule.type.name.lowercase(Locale.US)}:" +
                    "${matcherRule.pattern}:$parameter",
                pattern = matcherRule.pattern,
                type = matcherRule.type,
                parameterName = parameter,
                source = source,
                domainScope = matcherRule.domainScope
            )
        }

        private fun splitOptions(text: String): Pair<String, String?> {
            val optionsIndex = text.indexOf('$')
            if (optionsIndex < 0) {
                return text.trim() to null
            }
            return text.substring(0, optionsIndex).trim() to
                text.substring(optionsIndex + 1).trim().takeIf { it.isNotEmpty() }
        }

        private fun parseParameterName(value: String): String? {
            val parameter = value.trim()
            return parameter.takeIf { candidate ->
                candidate.length in 1..64 &&
                    PARAMETER_NAME_REGEX.matches(candidate)
            }
        }

        private val PARAMETER_NAME_REGEX = Regex("[A-Za-z0-9._~-]+")
    }
}
