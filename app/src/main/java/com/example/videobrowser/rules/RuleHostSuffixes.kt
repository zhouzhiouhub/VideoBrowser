package com.example.videobrowser.rules

import com.example.videobrowser.site.SiteHost

internal object RuleHostSuffixes {
    fun forHost(host: String?): List<String> {
        val normalizedHost = SiteHost.normalize(host) ?: return emptyList()
        val labels = normalizedHost.split('.').filter { label -> label.isNotEmpty() }
        return labels.indices.map { index ->
            labels.drop(index).joinToString(".")
        }
    }
}
