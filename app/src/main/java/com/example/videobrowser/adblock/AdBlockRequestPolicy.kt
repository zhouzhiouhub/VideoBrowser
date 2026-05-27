package com.example.videobrowser.adblock

/**
 * 请求级广告拦截策略，先处理开关、主文档和协议边界，再进入内置黑名单匹配。
 */
object AdBlockRequestPolicy {
    fun shouldBlock(
        enabled: Boolean,
        url: String,
        host: String?,
        scheme: String?,
        isForMainFrame: Boolean
    ): Boolean {
        // 主文档请求必须放行，避免广告黑名单误杀页面导航。
        if (!enabled || isForMainFrame || !isHttpScheme(scheme)) {
            return false
        }

        return BuiltInAdBlockRules.matches(url = url, host = host)
    }

    private fun isHttpScheme(scheme: String?): Boolean {
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true)
    }
}
