package com.example.videobrowser.browser

import android.webkit.WebResourceResponse
import com.example.videobrowser.adblock.EmptyResponseFactory

class SmartNoImageRequestInterceptor(
    private val isEnabled: () -> Boolean,
    private val isDisabledForCurrentSite: () -> Boolean,
    private val currentPageUrl: () -> String?
) {
    fun intercept(request: BrowserRequest): WebResourceResponse? {
        val context = RequestContext.from(
            request = request,
            pageUrl = request.pageUrl ?: currentPageUrl()
        )
        if (!SmartNoImageRequestPolicy.shouldBlock(
                enabled = isEnabled(),
                siteSmartNoImageDisabled = isDisabledForCurrentSite(),
                context = context
            )
        ) {
            return null
        }
        return EmptyResponseFactory.noContent()
    }
}
