package com.example.videobrowser.browser

enum class BottomBarButtonArrangement {
    VisibleActionsEvenlySpaced;

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun forVisibility(visibility: BottomBarButtonVisibility): BottomBarButtonArrangement {
            return VisibleActionsEvenlySpaced
        }
    }
}
