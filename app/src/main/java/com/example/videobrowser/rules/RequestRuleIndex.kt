package com.example.videobrowser.rules

import com.example.videobrowser.site.SiteHost

/**
 * 请求规则候选集索引。G2-03 只索引域名规则，URL contains / URL pattern 继续走 fallback，
 * 确保后续关键词索引接入前匹配结果仍和原始规则顺序一致。
 */
class RequestRuleIndex private constructor(
    private val domainCapabilitiesByActionAndSuffix: Map<RuleAction, Map<String, List<IndexedRequestCapability>>>,
    private val fallbackCapabilitiesByAction: Map<RuleAction, List<IndexedRequestCapability>>
) {
    fun candidatesFor(
        action: RuleAction,
        host: String?
    ): List<RuleCapability.Request> {
        val domainCandidates = hostSuffixes(host)
            .flatMap { suffix ->
                domainCapabilitiesByActionAndSuffix[action]
                    ?.get(suffix)
                    .orEmpty()
            }
        val fallbackCandidates = fallbackCapabilitiesByAction[action].orEmpty()
        return (domainCandidates + fallbackCandidates)
            .distinctBy { indexed -> indexed.order }
            .sortedBy { indexed -> indexed.order }
            .map { indexed -> indexed.capability }
    }

    companion object {
        val Empty = RequestRuleIndex(
            domainCapabilitiesByActionAndSuffix = emptyMap(),
            fallbackCapabilitiesByAction = emptyMap()
        )

        fun from(capabilities: List<RuleCapability.Request>): RequestRuleIndex {
            if (capabilities.isEmpty()) {
                return Empty
            }

            val domainCapabilities =
                mutableMapOf<RuleAction, MutableMap<String, MutableList<IndexedRequestCapability>>>()
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
                fallbackCapabilitiesByAction = fallbackCapabilities.mapValues { entry ->
                    entry.value.toList()
                }
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

private data class IndexedRequestCapability(
    val order: Int,
    val capability: RuleCapability.Request
)
