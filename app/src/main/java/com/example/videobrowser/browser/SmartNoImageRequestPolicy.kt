package com.example.videobrowser.browser

object SmartNoImageRequestPolicy {
    fun shouldBlock(
        enabled: Boolean,
        siteSmartNoImageDisabled: Boolean = false,
        context: RequestContext
    ): Boolean {
        return enabled &&
            !siteSmartNoImageDisabled &&
            !context.isForMainFrame &&
            isHttpScheme(context.requestScheme) &&
            context.resourceType == ResourceType.IMAGE
    }

    private fun isHttpScheme(scheme: String?): Boolean {
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true)
    }
}
