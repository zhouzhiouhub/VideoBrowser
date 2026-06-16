package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Progress Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DownloadProgressTest {
    /**
     * 测试函数 `percentUsesKnownTotal`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `percent Uses Known Total` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun percentUsesKnownTotal() {
        assertEquals(42, DownloadProgress(bytesDownloaded = 42L, totalBytes = 100L).percent())
    }

    /**
     * 测试函数 `percentCapsAtOneHundred`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `percent Caps At One Hundred` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun percentCapsAtOneHundred() {
        assertEquals(100, DownloadProgress(bytesDownloaded = 120L, totalBytes = 100L).percent())
    }

    /**
     * 测试函数 `percentIsUnknownWithoutTotal`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `percent Is Unknown Without Total` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun percentIsUnknownWithoutTotal() {
        assertNull(DownloadProgress(bytesDownloaded = 42L, totalBytes = -1L).percent())
        assertNull(DownloadProgress(bytesDownloaded = 42L, totalBytes = null).percent())
    }
}
