package com.example.videobrowser.functioncenter

enum class FunctionCenterRootSheetBlock {
    ACTION_GRID,
    HISTORY_PREVIEW,
    EXPANDED_BROWSER_SETTINGS,
    EXPANDED_DATA_MANAGEMENT
}

object FunctionCenterRootSheetLayout {
    fun blocks(): List<FunctionCenterRootSheetBlock> {
        return listOf(
            FunctionCenterRootSheetBlock.ACTION_GRID,
            FunctionCenterRootSheetBlock.HISTORY_PREVIEW
        )
    }
}
