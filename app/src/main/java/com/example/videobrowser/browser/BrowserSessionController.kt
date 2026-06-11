package com.example.videobrowser.browser

import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R

class BrowserSessionController(
    private val activity: AppCompatActivity,
    private val isActive: () -> Boolean,
    private val clearElementPickerState: () -> Unit,
    private val exitPageFullscreenIfNeeded: () -> Unit,
    private val isProviderHomeUrl: (String?) -> Boolean,
    private val updateAddressBar: (String?) -> Unit,
    private val showHomeContent: (Boolean) -> Unit,
    private val setPageProgress: (Int) -> Unit,
    private val updatePageProgressVisibility: (Boolean) -> Unit,
    private val updateNavigationButtons: () -> Unit,
    private val addHistoryEntry: (String?) -> Unit,
    private val injectPageFeatures: () -> Unit,
    private val onPageMetadataChanged: (String?, String?) -> Unit = { _, _ -> }
) {
    var currentPageTitle = ""
        private set

    @Volatile
    var currentPageUrl: String? = null

    var isPageLoading = false
        private set

    var isHomePageVisible = true
        private set

    private var pageProgress = 0

    fun handlePageStarted(url: String?) {
        if (isActive()) {
            clearElementPickerState()
            exitPageFullscreenIfNeeded()
        }
        currentPageUrl = url ?: currentPageUrl
        isHomePageVisible = isProviderHomeUrl(url)
        resetPageTitle()
        isPageLoading = true
        pageProgress = 0
        notifyPageMetadataChanged()
        renderIfActive()
    }

    fun handlePageFinished(url: String?) {
        currentPageUrl = url ?: currentPageUrl
        isHomePageVisible = isProviderHomeUrl(url)
        isPageLoading = false
        pageProgress = 100
        addHistoryEntry(url)
        notifyPageMetadataChanged()
        if (isActive()) {
            renderCurrentState(forceProgressHidden = true)
            injectPageFeatures()
        }
    }

    fun handlePageFailed(url: String?) {
        currentPageUrl = url ?: currentPageUrl
        isHomePageVisible = false
        isPageLoading = false
        pageProgress = 100
        notifyPageMetadataChanged()
        if (isActive()) {
            renderCurrentState(forceProgressHidden = true)
        }
    }

    fun handlePageProgressChanged(newProgress: Int) {
        val normalizedProgress = newProgress.coerceIn(0, 100)
        isPageLoading = normalizedProgress in 1..99
        pageProgress = normalizedProgress
        if (isActive()) {
            setPageProgress(pageProgress)
            updatePageProgressVisibility(false)
            updateNavigationButtons()
        }
    }

    fun handlePageTitleReceived(title: String) {
        val normalizedTitle = title.trim()
        currentPageTitle = normalizedTitle
        notifyPageMetadataChanged()
        updateActivityTitleIfActive()
    }

    fun renderCurrentState(forceProgressHidden: Boolean = !isPageLoading) {
        if (!isActive()) {
            return
        }
        updateActivityTitleIfActive()
        updateAddressBar(currentPageUrl)
        showHomeContent(isHomePageVisible)
        setPageProgress(pageProgress)
        updatePageProgressVisibility(forceProgressHidden)
        updateNavigationButtons()
    }

    fun reset() {
        currentPageTitle = ""
        currentPageUrl = null
        isPageLoading = false
        isHomePageVisible = true
        pageProgress = 0
        notifyPageMetadataChanged()
        renderCurrentState(forceProgressHidden = true)
    }

    private fun resetPageTitle() {
        currentPageTitle = ""
        updateActivityTitleIfActive()
    }

    private fun renderIfActive() {
        if (isActive()) {
            renderCurrentState(forceProgressHidden = false)
        }
    }

    private fun updateActivityTitleIfActive() {
        if (!isActive()) {
            return
        }
        activity.title = currentPageTitle.takeIf { it.isNotBlank() }
            ?: activity.getString(R.string.app_name)
    }

    private fun notifyPageMetadataChanged() {
        onPageMetadataChanged(currentPageUrl, currentPageTitle)
    }
}
