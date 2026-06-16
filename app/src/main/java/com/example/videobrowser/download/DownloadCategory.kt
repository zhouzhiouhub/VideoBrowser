package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadCategory 可以拆开理解为“Download Category”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
enum class DownloadCategory {
    VIDEO,
    IMAGE,
    AUDIO,
    DOCUMENT,
    APP,
    ARCHIVE,
    OTHER;

    companion object {
        /**
         * 函数 `from`：封装 `from` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param mimeType 参数类型为 `String?`，表示函数执行 `mimeType` 相关逻辑时需要读取或处理的输入。
         * @param fileName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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
        /**
         * 函数 `from`：封装 `from` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param records 参数类型为 `List<DownloadRecord>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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
