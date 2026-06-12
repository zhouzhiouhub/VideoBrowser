package com.example.videobrowser.download

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadSafetyPolicyTest {
    @Test
    fun requiresConfirmationForAndroidAppPackages() {
        assertTrue(
            DownloadSafetyPolicy.requiresConfirmation(
                fileName = "release.apk",
                mimeType = null
            )
        )
        assertTrue(
            DownloadSafetyPolicy.requiresConfirmation(
                fileName = "package.zip",
                mimeType = "application/vnd.android.package-archive"
            )
        )
        assertTrue(
            DownloadSafetyPolicy.requiresConfirmation(
                fileName = "bundle.xapk",
                mimeType = "application/octet-stream"
            )
        )
    }

    @Test
    fun skipsConfirmationForOrdinaryDownloads() {
        assertFalse(
            DownloadSafetyPolicy.requiresConfirmation(
                fileName = "video.mp4",
                mimeType = "video/mp4"
            )
        )
        assertFalse(
            DownloadSafetyPolicy.requiresConfirmation(
                fileName = "archive.zip",
                mimeType = "application/zip"
            )
        )
        assertFalse(
            DownloadSafetyPolicy.requiresConfirmation(
                fileName = "document.pdf",
                mimeType = "application/pdf"
            )
        )
    }

    @Test
    fun requiresInsecureTransportConfirmationOnlyFromHttpsPagesToHttpDownloads() {
        assertTrue(
            DownloadSafetyPolicy.requiresInsecureTransportConfirmation(
                pageUrl = "https://secure.example.com/page",
                downloadUrl = "http://downloads.example.com/file.zip"
            )
        )
        assertFalse(
            DownloadSafetyPolicy.requiresInsecureTransportConfirmation(
                pageUrl = "https://secure.example.com/page",
                downloadUrl = "https://downloads.example.com/file.zip"
            )
        )
        assertFalse(
            DownloadSafetyPolicy.requiresInsecureTransportConfirmation(
                pageUrl = "http://plain.example.com/page",
                downloadUrl = "http://downloads.example.com/file.zip"
            )
        )
        assertFalse(
            DownloadSafetyPolicy.requiresInsecureTransportConfirmation(
                pageUrl = null,
                downloadUrl = "http://downloads.example.com/file.zip"
            )
        )
    }
}
