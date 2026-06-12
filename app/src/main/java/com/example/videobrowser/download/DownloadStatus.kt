package com.example.videobrowser.download

enum class DownloadStatus(val storageValue: String) {
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    FAILED("failed"),
    CANCELED("canceled");

    companion object {
        fun fromStorage(value: String): DownloadStatus? {
            val normalized = value.trim()
            return values().firstOrNull { status ->
                status.storageValue.equals(normalized, ignoreCase = true) ||
                    status.name.equals(normalized, ignoreCase = true)
            }
        }
    }
}
