package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 RequestRuleIndex 可以拆开理解为“Request Rule Index”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.site.SiteHost
import java.util.Locale

/**
 * 请求规则候选集索引。域名规则按 host 后缀取候选，URL contains 规则按稳定关键词取候选；
 * URL pattern 和无法提取关键词的短规则继续走 fallback，保证索引结果不改变原始规则语义。
 */
class RequestRuleIndex private constructor(
    private val domainCapabilitiesByActionAndSuffix: Map<RuleAction, Map<String, List<IndexedRequestCapability>>>,
    private val urlContainsCapabilitiesByActionAndKeyword: Map<RuleAction, Map<String, List<IndexedRequestCapability>>>,
    private val urlContainsCapabilitiesByAction: Map<RuleAction, List<IndexedRequestCapability>>,
    private val fallbackCapabilitiesByAction: Map<RuleAction, List<IndexedRequestCapability>>
) {
    /**
     * 函数 `candidatesFor`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param action 参数类型为 `RuleAction`，表示函数执行 `action` 相关逻辑时需要读取或处理的输入。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun candidatesFor(
        action: RuleAction,
        host: String?,
        url: String? = null
    ): List<RuleCapability.Request> {
        val domainCandidates = RuleHostSuffixes.forHost(host)
            .flatMap { suffix ->
                domainCapabilitiesByActionAndSuffix[action]
                    ?.get(suffix)
                    .orEmpty()
            }
        val urlContainsCandidates = urlContainsCandidatesFor(
            action = action,
            url = url
        )
        val fallbackCandidates = fallbackCapabilitiesByAction[action].orEmpty()
        return (domainCandidates + urlContainsCandidates + fallbackCandidates)
            .distinctBy { indexed -> indexed.order }
            .sortedBy { indexed -> indexed.order }
            .map { indexed -> indexed.capability }
    }

    /**
     * 函数 `urlContainsCandidatesFor`：封装 `url Contains Candidates For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param action 参数类型为 `RuleAction`，表示函数执行 `action` 相关逻辑时需要读取或处理的输入。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun urlContainsCandidatesFor(
        action: RuleAction,
        url: String?
    ): List<IndexedRequestCapability> {
        val candidatesByKeyword = urlContainsCapabilitiesByActionAndKeyword[action].orEmpty()
        if (url == null) {
            // 兼容测试和诊断调用：没有 URL 时不裁剪 URL contains 候选。
            return urlContainsCapabilitiesByAction[action].orEmpty()
        }

        return indexKeysForUrl(url)
            .flatMap { key -> candidatesByKeyword[key].orEmpty() }
    }

    companion object {
        val Empty = RequestRuleIndex(
            domainCapabilitiesByActionAndSuffix = emptyMap(),
            urlContainsCapabilitiesByActionAndKeyword = emptyMap(),
            urlContainsCapabilitiesByAction = emptyMap(),
            fallbackCapabilitiesByAction = emptyMap()
        )

        /**
         * 函数 `from`：封装 `from` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param capabilities 参数类型为 `List<RuleCapability.Request>`，表示函数执行 `capabilities` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun from(capabilities: List<RuleCapability.Request>): RequestRuleIndex {
            if (capabilities.isEmpty()) {
                return Empty
            }

            val domainCapabilities =
                mutableMapOf<RuleAction, MutableMap<String, MutableList<IndexedRequestCapability>>>()
            val urlContainsCapabilities =
                mutableMapOf<RuleAction, MutableMap<String, MutableList<IndexedRequestCapability>>>()
            val allUrlContainsCapabilities = mutableMapOf<RuleAction, MutableList<IndexedRequestCapability>>()
            val fallbackCapabilities = mutableMapOf<RuleAction, MutableList<IndexedRequestCapability>>()

            capabilities.forEachIndexed { index, capability ->
                val indexedCapability = IndexedRequestCapability(
                    order = index,
                    capability = capability
                )
                val rule = capability.rule
                val suffix = SiteHost.normalize(rule.pattern)
                if (rule.type == RuleType.DOMAIN_CONTAINS && suffix != null) {
                    domainCapabilities
                        .getOrPut(rule.action) { mutableMapOf() }
                        .getOrPut(suffix) { mutableListOf() }
                        .add(indexedCapability)
                } else if (rule.type == RuleType.URL_CONTAINS) {
                    val keyword = stableKeywordFor(rule.normalizedPattern)
                    if (keyword == null) {
                        fallbackCapabilities
                            .getOrPut(rule.action) { mutableListOf() }
                            .add(indexedCapability)
                    } else {
                        // 只用关键词缩小候选集，完整 URL、资源类型和站点限制仍由 RuleMatcher 校验。
                        urlContainsCapabilities
                            .getOrPut(rule.action) { mutableMapOf() }
                            .getOrPut(keyword) { mutableListOf() }
                            .add(indexedCapability)
                        allUrlContainsCapabilities
                            .getOrPut(rule.action) { mutableListOf() }
                            .add(indexedCapability)
                    }
                } else {
                    fallbackCapabilities
                        .getOrPut(rule.action) { mutableListOf() }
                        .add(indexedCapability)
                }
            }

            return RequestRuleIndex(
                domainCapabilitiesByActionAndSuffix = domainCapabilities.mapValues { entry ->
                    entry.value.mapValues { suffixEntry -> suffixEntry.value.toList() }
                },
                urlContainsCapabilitiesByActionAndKeyword = urlContainsCapabilities.mapValues { entry ->
                    entry.value.mapValues { keywordEntry -> keywordEntry.value.toList() }
                },
                urlContainsCapabilitiesByAction = allUrlContainsCapabilities.mapValues { entry ->
                    entry.value.toList()
                },
                fallbackCapabilitiesByAction = fallbackCapabilities.mapValues { entry ->
                    entry.value.toList()
                }
            )
        }

        /**
         * 函数 `stableKeywordFor`：封装 `stable Keyword For` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param pattern 参数类型为 `String`，表示函数执行 `pattern` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun stableKeywordFor(pattern: String): String? {
            val longestToken = alphanumericTokens(pattern)
                .maxByOrNull { keyword -> keyword.length }
                ?: return null
            return longestToken.take(INDEX_KEY_LENGTH)
        }

        /**
         * 函数 `alphanumericTokens`：封装 `alphanumeric Tokens` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun alphanumericTokens(text: String): List<String> {
            val tokens = mutableListOf<String>()
            val builder = StringBuilder()
            text.trim().lowercase(Locale.US).forEach { char ->
                if (char.isLetterOrDigit()) {
                    builder.append(char)
                } else {
                    flushKeyword(builder, tokens)
                }
            }
            flushKeyword(builder, tokens)
            return tokens.distinct()
        }

        /**
         * 函数 `indexKeysForUrl`：封装 `index Keys For Url` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun indexKeysForUrl(url: String): List<String> {
            val normalizedUrl = url.trim().lowercase(Locale.US)
            if (normalizedUrl.length < INDEX_KEY_LENGTH) {
                return emptyList()
            }
            return (0..normalizedUrl.length - INDEX_KEY_LENGTH)
                .map { index -> normalizedUrl.substring(index, index + INDEX_KEY_LENGTH) }
                .distinct()
        }

        /**
         * 函数 `flushKeyword`：封装 `flush Keyword` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param builder 参数类型为 `StringBuilder`，表示函数执行 `builder` 相关逻辑时需要读取或处理的输入。
         * @param tokens 参数类型为 `MutableList<String>`，表示函数执行 `tokens` 相关逻辑时需要读取或处理的输入。
         */
        private fun flushKeyword(
            builder: StringBuilder,
            tokens: MutableList<String>
        ) {
            if (builder.length >= MIN_KEYWORD_LENGTH) {
                tokens += builder.toString()
            }
            builder.clear()
        }

        private const val MIN_KEYWORD_LENGTH = 3
        private const val INDEX_KEY_LENGTH = 3
    }
}

private data class IndexedRequestCapability(
    val order: Int,
    val capability: RuleCapability.Request
)
