package com.example.videobrowser.browser

/**
 * Keeps pages hidden until app-owned page feature scripts have applied the
 * cleanup that should be invisible to users.
 */
class BrowserPageFeatureVisibilityController(
    private val setActiveWebViewAlpha: (Float) -> Unit,
    private val shouldHideUntilPageFeaturesInjected: (String?) -> Boolean
) {
    private var hiddenPageUrl: String? = null

    fun handlePageStarted(url: String?) {
        if (shouldHideUntilPageFeaturesInjected(url)) {
            hiddenPageUrl = normalizedUrl(url)
            setActiveWebViewAlpha(HIDDEN_ALPHA)
        } else {
            reveal()
        }
    }

    fun handlePageFeaturesInjected(url: String?) {
        if (hiddenPageUrl != null && isHiddenPageUrl(url)) {
            reveal()
        }
    }

    fun handlePageFailed(url: String?) {
        if (hiddenPageUrl != null && isHiddenPageUrl(url)) {
            reveal()
        }
    }

    private fun reveal() {
        hiddenPageUrl = null
        setActiveWebViewAlpha(VISIBLE_ALPHA)
    }

    private fun isHiddenPageUrl(url: String?): Boolean {
        val hiddenUrl = hiddenPageUrl ?: return false
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
