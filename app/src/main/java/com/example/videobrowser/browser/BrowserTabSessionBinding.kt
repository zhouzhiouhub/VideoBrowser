package com.example.videobrowser.browser

class BrowserTabSessionBinding(
    private val tabs: BrowserTabStore
) {
    fun handlePageMetadataChanged(url: String?, title: String?) {
        tabs.updateActiveTab(
            url = url,
            title = title ?: tabs.activeTab().title
        )
    }
}
