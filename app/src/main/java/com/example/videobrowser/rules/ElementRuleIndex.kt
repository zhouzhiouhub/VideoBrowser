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
