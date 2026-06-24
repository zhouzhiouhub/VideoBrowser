package com.example.videobrowser.browser

import com.example.videobrowser.utils.WebPageIdentity

/**
 * 判断哪些网页 URL 应该被视为 App 自定义首页，而不是需要展示的网页内容。
 */
class BrowserHomePageUrlPolicy(
    private val homeUrls: () -> List<String>
) {
    fun isHomeUrl(url: String?): Boolean {
        val currentUrl = WebPageIdentity.from(url) ?: return false
        return homeUrls()
            .mapNotNull(WebPageIdentity::from)
            .any { homeUrl -> homeUrl.isSamePageAs(currentUrl) }
    }
}
