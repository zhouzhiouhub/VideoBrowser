package com.example.videobrowser.browser

data class BottomBarButtonVisibility(
    val showBack: Boolean,
    val showPageTools: Boolean,
    val showRefresh: Boolean = true,
    val showWenxin: Boolean = true,
    val showProfile: Boolean = true
) {
    companion object {
        fun forPageState(isHomePageVisible: Boolean): BottomBarButtonVisibility {
            val showPageNavigation = !isHomePageVisible
            return BottomBarButtonVisibility(
                showBack = showPageNavigation,
                showPageTools = showPageNavigation
            )
        }
    }
}
