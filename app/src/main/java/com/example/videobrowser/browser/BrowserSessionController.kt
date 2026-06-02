package com.example.videobrowser.browser

import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R

class BrowserSessionController(
    private val activity: AppCompatActivity,
    private val clearElementPickerState: () -> Unit,
    private val exitPageFullscreenIfNeeded: () -> Unit,
    private val isProviderHomeUrl: (String?) -> Boolean,
    private val updateAddressBar: (String?) -> Unit,
    private val showHomeContent: (Boolean) -> Unit,
    private val setPageProgress: (Int) -> Unit,
    private val updatePageProgressVisibility: (Boolean) -> Unit,
    private val updateNavigationButtons: () -> Unit,
    private val addHistoryEntry: (String?) -> Unit,
    private val injectPageFeatures: () -> Unit
) {
    var currentPageTitle = ""
        private set

    @Volatile
    var currentPageUrl: String? = null

    var isPageLoading = false
        private set

    fun handlePageStarted(url: String?) {
        clearElementPickerState()
        currentPageUrl = url ?: currentPageUrl
        exitPageFullscreenIfNeeded()
        val isProviderHome = isProviderHomeUrl(url)
        resetPageTitle()
        updateAddressBar(url)
        showHomeContent(isProviderHome)
        isPageLoading = true
        setPageProgress(0)
        updatePageProgressVisibility(false)
        updateNavigationButtons()
    }

    fun handlePageFinished(url: String?) {
        currentPageUrl = url ?: currentPageUrl
        val isProviderHome = isProviderHomeUrl(url)
        updateAddressBar(url)
        showHomeContent(isProviderHome)
        isPageLoading = false
        setPageProgress(100)
        updatePageProgressVisibility(true)
        addHistoryEntry(url)
        injectPageFeatures()
        updateNavigationButtons()
    }

    fun handlePageProgressChanged(newProgress: Int) {
        val normalizedProgress = newProgress.coerceIn(0, 100)
        isPageLoading = normalizedProgress in 1..99
        setPageProgress(normalizedProgress)
        updatePageProgressVisibility(false)
        updateNavigationButtons()
    }

    fun handlePageTitleReceived(title: String) {
        val normalizedTitle = title.trim()
        currentPageTitle = normalizedTitle
        activity.title = normalizedTitle.takeIf { it.isNotBlank() }
            ?: activity.getString(R.string.app_name)
    }

    private fun resetPageTitle() {
        currentPageTitle = ""
        activity.title = activity.getString(R.string.app_name)
    }
}
