package com.example.videobrowser.browser

data class BrowserTab(
    val id: Long,
    val url: String? = null,
    val title: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)
