package com.example.videobrowser.browser

object LocalWebArchivePolicy {
    private val webArchiveExtensions = setOf("mht", "mhtml")
    private val webArchiveMimeTypes = setOf(
        "application/mhtml",
        "application/x-mht",
        "application/x-mhtml",
        "application/x-mimearchive",
        "multipart/related"
    )

    fun isWebArchive(displayName: String?, mimeType: String?): Boolean {
        val extension = displayName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            .orEmpty()
        val normalizedMimeType = mimeType
            ?.substringBefore(';')
            ?.trim()
            ?.lowercase()
            .orEmpty()

        return extension in webArchiveExtensions || normalizedMimeType in webArchiveMimeTypes
    }
}
