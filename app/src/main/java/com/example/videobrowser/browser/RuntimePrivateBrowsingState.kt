package com.example.videobrowser.browser

class RuntimePrivateBrowsingState(
    private val onPrivateCleanup: () -> Unit = {}
) {
    var mode: BrowserMode = BrowserMode.STANDARD
        private set

    val isPrivate: Boolean
        get() = mode == BrowserMode.PRIVATE

    fun enterPrivate(): Boolean {
        if (isPrivate) {
            return false
        }
        mode = BrowserMode.PRIVATE
        return true
    }

    fun exitPrivate(): Boolean {
        if (!isPrivate) {
            return false
        }
        onPrivateCleanup()
        mode = BrowserMode.STANDARD
        return true
    }

    fun resetToStandard() {
        if (isPrivate) {
            onPrivateCleanup()
        }
        mode = BrowserMode.STANDARD
    }
}
