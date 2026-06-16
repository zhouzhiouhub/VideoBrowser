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
    /**
     * 测试函数 `isWebArchiveMatchesMhtmlFileNames`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `is Web Archive Matches Mhtml File Names` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun isWebArchiveMatchesMhtmlFileNames() {
        assertTrue(LocalWebArchivePolicy.isWebArchive("Saved Page.mhtml", null))
        assertTrue(LocalWebArchivePolicy.isWebArchive("Saved Page.MHT", null))
    }

    /**
     * 测试函数 `isWebArchiveMatchesKnownMimeTypes`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `is Web Archive Matches Known Mime Types` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun isWebArchiveMatchesKnownMimeTypes() {
        assertTrue(LocalWebArchivePolicy.isWebArchive("download", "multipart/related; boundary=page"))
        assertTrue(LocalWebArchivePolicy.isWebArchive("download", "application/x-mimearchive"))
    }

    /**
     * 测试函数 `isWebArchiveRejectsOrdinaryDocuments`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `is Web Archive Rejects Ordinary Documents` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun isWebArchiveRejectsOrdinaryDocuments() {
        assertFalse(LocalWebArchivePolicy.isWebArchive("report.pdf", "application/pdf"))
        assertFalse(LocalWebArchivePolicy.isWebArchive("page.html", "text/html"))
    }
}
