package com.example.videobrowser.storage

data class SavedPage(
    val title: String,
    val url: String,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L,
    val folder: String = ""
)

data class BookmarkImportResult(
    val importedCount: Int,
    val skippedCount: Int
)
