package com.example.videobrowser.browser.search

/**
 * Keeps built-in search result pages hidden until the app-owned search shell
 * cleanup has been applied.
 */
class BuiltInSearchResultPageVisibilityController(
    private val setActiveWebViewAlpha: (Float) -> Unit,
    private val isBuiltInSearchResultUrl: (String?) -> Boolean
) {
    private var hiddenSearchResultUrl: String? = null

    fun handlePageStarted(url: String?) {
        if (isBuiltInSearchResultUrl(url)) {
            hiddenSearchResultUrl = normalizedUrl(url)
            setActiveWebViewAlpha(HIDDEN_ALPHA)
        } else {
            reveal()
        }
    }

    fun handlePageFeaturesInjected(url: String?) {
        if (hiddenSearchResultUrl != null && isHiddenSearchResultUrl(url)) {
            reveal()
        }
    }

    fun handlePageFailed(url: String?) {
        if (hiddenSearchResultUrl != null && isHiddenSearchResultUrl(url)) {
            reveal()
        }
    }

    private fun reveal() {
        hiddenSearchResultUrl = null
        setActiveWebViewAlpha(VISIBLE_ALPHA)
    }

    private fun isHiddenSearchResultUrl(url: String?): Boolean {
        val hiddenUrl = hiddenSearchResultUrl ?: return false
        val currentUrl = normalizedUrl(url)
        return currentUrl == null || currentUrl == hiddenUrl
    }

    private fun normalizedUrl(url: String?): String? {
        return url?.trim()?.takeIf { value -> value.isNotEmpty() }
    }

    private companion object {
        private const val HIDDEN_ALPHA = 0f
        private const val VISIBLE_ALPHA = 1f
    }
}
