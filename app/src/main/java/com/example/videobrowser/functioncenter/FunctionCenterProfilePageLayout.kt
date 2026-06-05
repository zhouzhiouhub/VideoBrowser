package com.example.videobrowser.functioncenter

enum class FunctionCenterProfilePageBlock {
    PROFILE_HEADER,
    SHORTCUTS,
    FEATURES
}

object FunctionCenterProfilePageLayout {
    fun blocks(): List<FunctionCenterProfilePageBlock> {
        return listOf(
            FunctionCenterProfilePageBlock.SHORTCUTS,
            FunctionCenterProfilePageBlock.FEATURES
        )
    }
}
