package com.example.videobrowser.browser

enum class BottomBarButtonArrangement {
    HomeSplit,
    BrowsingEvenlySpaced;

    companion object {
        fun forVisibility(visibility: BottomBarButtonVisibility): BottomBarButtonArrangement {
            return if (visibility.showBack || visibility.showPageTools) {
                BrowsingEvenlySpaced
            } else {
                HomeSplit
            }
        }
    }
}
