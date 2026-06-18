package com.example.videobrowser.functioncenter

data class FunctionCenterGridAction(
    val title: String,
    val summary: String,
    val iconResId: Int,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)
