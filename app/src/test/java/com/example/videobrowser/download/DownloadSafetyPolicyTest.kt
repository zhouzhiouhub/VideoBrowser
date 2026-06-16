package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Safety Policy Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
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

    @Test
    fun isDownloadableNetworkUrlOnlyAllowsHttpAndHttpsWithHosts() {
        assertTrue(DownloadSafetyPolicy.isDownloadableNetworkUrl("https://example.com/file.zip"))
        assertTrue(DownloadSafetyPolicy.isDownloadableNetworkUrl(" http://downloads.example.com/file.zip "))
        assertFalse(DownloadSafetyPolicy.isDownloadableNetworkUrl("data:text/plain,hello"))
        assertFalse(DownloadSafetyPolicy.isDownloadableNetworkUrl("file:///sdcard/Download/file.zip"))
        assertFalse(DownloadSafetyPolicy.isDownloadableNetworkUrl("javascript:alert(1)"))
        assertFalse(DownloadSafetyPolicy.isDownloadableNetworkUrl("https:/missing-host/file.zip"))
    }

    @Test
    fun safeDownloadFileName_removesPathSeparatorsAndInvalidCharacters() {
        assertEquals("movie.mp4", DownloadSafetyPolicy.safeDownloadFileName(" movie.mp4 "))
        assertEquals("_evil.apk", DownloadSafetyPolicy.safeDownloadFileName("../evil.apk"))
        assertEquals("folder_file_.zip", DownloadSafetyPolicy.safeDownloadFileName("folder\\file?.zip"))
        assertEquals("report_final.pdf", DownloadSafetyPolicy.safeDownloadFileName("report\tfinal.pdf"))
    }

    @Test
    fun safeDownloadFileName_fallsBackWhenNameIsBlankAfterSanitizing() {
        assertEquals("download.bin", DownloadSafetyPolicy.safeDownloadFileName("...   "))
    }

    @Test
    fun safeDownloadFileName_limitsLongNames() {
        val name = "a".repeat(200)

        assertEquals(120, DownloadSafetyPolicy.safeDownloadFileName(name).length)
    }
}
