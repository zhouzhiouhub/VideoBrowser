package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Local Web Archive Policy Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalWebArchivePolicyTest {
    @Test
    fun isWebArchiveMatchesMhtmlFileNames() {
        assertTrue(LocalWebArchivePolicy.isWebArchive("Saved Page.mhtml", null))
        assertTrue(LocalWebArchivePolicy.isWebArchive("Saved Page.MHT", null))
    }

    @Test
    fun isWebArchiveMatchesKnownMimeTypes() {
        assertTrue(LocalWebArchivePolicy.isWebArchive("download", "multipart/related; boundary=page"))
        assertTrue(LocalWebArchivePolicy.isWebArchive("download", "application/x-mimearchive"))
    }

    @Test
    fun isWebArchiveRejectsOrdinaryDocuments() {
        assertFalse(LocalWebArchivePolicy.isWebArchive("report.pdf", "application/pdf"))
        assertFalse(LocalWebArchivePolicy.isWebArchive("page.html", "text/html"))
    }
}
