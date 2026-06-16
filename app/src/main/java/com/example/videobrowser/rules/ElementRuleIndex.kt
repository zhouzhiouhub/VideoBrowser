package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 ElementRuleIndex 可以拆开理解为“Element Rule Index”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.site.SiteHost

/**
 * CSS / DOM 页面规则候选集索引。带 include domain 的规则按页面 host 后缀取候选；
 * 全局规则和仅带 exclude domain 的规则保留在 fallback，再由 ElementRule.matchesPage 做最终校验。
 */
class ElementRuleIndex<T : RuleCapability.PageMutation> private constructor(
    private val capabilitiesByPageHostSuffix: Map<String, List<IndexedElementCapability<T>>>,
    private val fallbackCapabilities: List<IndexedElementCapability<T>>
) {
    /**
     * 函数 `candidatesFor`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun candidatesFor(pageHost: String?): List<T> {
        val scopedCandidates = hostSuffixes(pageHost)
            .flatMap { suffix ->
                capabilitiesByPageHostSuffix[suffix].orEmpty()
            }
        return (scopedCandidates + fallbackCapabilities)
            .distinctBy { indexed -> indexed.order }
            .sortedBy { indexed -> indexed.order }
            .map { indexed -> indexed.capability }
    }

    companion object {
        /**
         * 函数 `from`：封装 `from` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param capabilities 参数类型为 `List<T>`，表示函数执行 `capabilities` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun <T : RuleCapability.PageMutation> from(
            capabilities: List<T>
        ): ElementRuleIndex<T> {
            if (capabilities.isEmpty()) {
                return ElementRuleIndex(
                    capabilitiesByPageHostSuffix = emptyMap(),
                    fallbackCapabilities = emptyList()
                )
            }

            val capabilitiesByHostSuffix = mutableMapOf<String, MutableList<IndexedElementCapability<T>>>()
            val fallbackCapabilities = mutableListOf<IndexedElementCapability<T>>()

            capabilities.forEachIndexed { index, capability ->
                val indexedCapability = IndexedElementCapability(
                    order = index,
                    capability = capability
                )
                val includedDomains = capability.rule.normalizedDomains
                if (includedDomains.isEmpty()) {
                    fallbackCapabilities += indexedCapability
                } else {
                    includedDomains.forEach { domain ->
                        capabilitiesByHostSuffix
                            .getOrPut(domain) { mutableListOf() }
                            .add(indexedCapability)
                    }
                }
            }

            return ElementRuleIndex(
                capabilitiesByPageHostSuffix = capabilitiesByHostSuffix.mapValues { entry ->
                    entry.value.toList()
                },
                fallbackCapabilities = fallbackCapabilities.toList()
            )
        }

        /**
         * 函数 `hostSuffixes`：封装 `host Suffixes` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        private fun hostSuffixes(host: String?): List<String> {
            val normalizedHost = SiteHost.normalize(host) ?: return emptyList()
            val labels = normalizedHost.split('.').filter { label -> label.isNotEmpty() }
            return labels.indices.map { index ->
                labels.drop(index).joinToString(".")
            }
        }
    }
}

private data class IndexedElementCapability<T : RuleCapability.PageMutation>(
    val order: Int,
    val capability: T
)
