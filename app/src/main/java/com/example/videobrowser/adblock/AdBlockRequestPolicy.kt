package com.example.videobrowser.adblock

import com.example.videobrowser.rules.RuleEngine

/**
 * 请求级广告拦截策略，先处理开关、主文档和协议边界，再进入规则系统匹配。
 */
object AdBlockRequestPolicy {
    fun shouldBlock(
        enabled: Boolean,
        url: String,
        host: String?,
        scheme: String?,
        isForMainFrame: Boolean,
        ruleEngine: RuleEngine
    ): Boolean {
        // 主文档请求必须放行，避免广告黑名单误杀页面导航。
        if (!enabled || isForMainFrame || !isHttpScheme(scheme)) {
            return false
        }

        return ruleEngine.matchRequest(url = url, host = host).shouldBlock
    }

    private fun isHttpScheme(scheme: String?): Boolean {
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true)
    }
}
