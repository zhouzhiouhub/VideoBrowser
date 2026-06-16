package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Cancellation Policy Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadCancellationPolicyTest {
    /**
     * 测试函数 `onlyInProgressDownloadsCanBeCanceled`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `only In Progress Downloads Can Be Canceled` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun onlyInProgressDownloadsCanBeCanceled() {
        assertTrue(DownloadCancellationPolicy.canCancel(record(DownloadStatus.IN_PROGRESS)))
        assertFalse(DownloadCancellationPolicy.canCancel(record(DownloadStatus.COMPLETED)))
        assertFalse(DownloadCancellationPolicy.canCancel(record(DownloadStatus.FAILED)))
        assertFalse(DownloadCancellationPolicy.canCancel(record(DownloadStatus.CANCELED)))
    }

    /**
     * 测试函数 `record`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `record` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param status 参数类型为 `DownloadStatus`，表示函数执行 `status` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun record(status: DownloadStatus): DownloadRecord {
        return DownloadRecord(
            downloadId = 1L,
            title = "file.zip",
            sourceUrl = "https://example.com/file.zip",
            fileName = "file.zip",
            mimeType = "application/zip",
            createdAtMillis = 1L,
            status = status
        )
    }
}
