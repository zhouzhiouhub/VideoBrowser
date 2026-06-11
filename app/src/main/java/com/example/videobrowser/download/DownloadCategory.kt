package com.example.videobrowser.download

enum class DownloadCategory {
    VIDEO,
    IMAGE,
    AUDIO,
    DOCUMENT,
    APP,
    ARCHIVE,
    OTHER;

    companion object {
        fun from(mimeType: String?, fileName: String): DownloadCategory {
            val normalizedMimeType = mimeType
                ?.substringBefore(';')
                ?.trim()
                ?.lowercase()
                .orEmpty()
            val extension = fileName
                .substringAfterLast('.', missingDelimiterValue = "")
                .lowercase()

            return when {
                normalizedMimeType.startsWith("video/") ||
                    extension in VIDEO_EXTENSIONS -> VIDEO
                normalizedMimeType.startsWith("image/") ||
                    extension in IMAGE_EXTENSIONS -> IMAGE
                normalizedMimeType.startsWith("audio/") ||
                    extension in AUDIO_EXTENSIONS -> AUDIO
                normalizedMimeType == APK_MIME_TYPE ||
                    extension in APP_EXTENSIONS -> APP
                normalizedMimeType in ARCHIVE_MIME_TYPES ||
                    extension in ARCHIVE_EXTENSIONS -> ARCHIVE
                normalizedMimeType.startsWith("text/") ||
                    normalizedMimeType in DOCUMENT_MIME_TYPES ||
                    extension in DOCUMENT_EXTENSIONS -> DOCUMENT
                else -> OTHER
            }
        }

        private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        private val VIDEO_EXTENSIONS = setOf(
            "mp4",
            "m4v",
            "webm",
            "mkv",
            "mov",
            "3gp",
            "ts",
            "mpeg",
            "mpg",
            "flv",
            "m3u8",
            "mpd"
        )
        private val IMAGE_EXTENSIONS = setOf(
            "jpg",
            "jpeg",
            "png",
            "gif",
            "webp",
            "bmp",
            "svg",
            "avif",
            "heic",
            "heif"
        )
        private val AUDIO_EXTENSIONS = setOf(
            "mp3",
            "m4a",
            "aac",
            "wav",
            "ogg",
            "opus",
            "flac"
        )
        private val DOCUMENT_EXTENSIONS = setOf(
            "pdf",
            "doc",
            "docx",
            "xls",
            "xlsx",
            "ppt",
            "pptx",
            "txt",
            "html",
            "htm",
            "md",
            "csv",
            "epub"
        )
        private val APP_EXTENSIONS = setOf("apk", "xapk", "apks")
        private val ARCHIVE_EXTENSIONS = setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")
        private val DOCUMENT_MIME_TYPES = setOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/epub+zip"
        )
        private val ARCHIVE_MIME_TYPES = setOf(
            "application/zip",
            "application/x-7z-compressed",
            "application/x-rar-compressed",
            "application/x-tar",
            "application/gzip",
            "application/x-bzip2",
            "application/x-xz"
        )
    }
}

data class DownloadCategoryGroup(
    val category: DownloadCategory,
    val records: List<DownloadRecord>
) {
    companion object {
        fun from(records: List<DownloadRecord>): List<DownloadCategoryGroup> {
            val recordsByCategory = records.groupBy { record ->
                DownloadCategory.from(record.mimeType, record.fileName)
            }
            return DownloadCategory.entries.mapNotNull { category ->
                recordsByCategory[category]
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { categoryRecords ->
                        DownloadCategoryGroup(category, categoryRecords)
                    }
            }
        }
    }
}
