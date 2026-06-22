package com.example.videobrowser.download

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Safety Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadSafetyWiringContractTest {
    /**
     * 测试函数 `downloadControllerConfirmsRiskyApplicationPackagesBeforeEnqueueing`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `download Controller Confirms Risky Application Packages Before Enqueueing` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun downloadControllerConfirmsRiskyApplicationPackagesBeforeEnqueueing() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadController.kt"
        ).readText()
        val enqueueController = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadEnqueueController.kt"
        ).readText()
        val policy = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadSafetyPolicy.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(policy.contains("object DownloadSafetyPolicy"))
        assertTrue(policy.contains("DownloadCategory.APP"))
        assertTrue(policy.contains("requiresInsecureTransportConfirmation(pageUrl: String?, downloadUrl: String)"))
        assertTrue(policy.contains("WebSchemePolicy.isHttpsScheme(SafeUriParser.scheme(pageUrl))"))
        assertTrue(policy.contains("WebSchemePolicy.isHttpScheme(SafeUriParser.scheme(downloadUrl))"))
        assertTrue(policy.contains("fun isDownloadableNetworkUrl(url: String): Boolean"))
        assertTrue(policy.contains("WebSchemePolicy.isHttpOrHttpsScheme(uri.scheme)"))
        assertTrue(policy.contains("fun safeDownloadFileName(fileName: String): String"))
        assertTrue(policy.contains("FileNameSanitizer.replaceInvalidCharacters"))
        assertTrue(controller.contains("DownloadEnqueueController("))
        assertTrue(controller.contains("enqueueController.enqueue("))
        assertTrue(enqueueController.contains("DownloadSafetyPolicy.requiresConfirmation(fileName, mimeType)"))
        assertTrue(enqueueController.contains("DownloadSafetyPolicy.isDownloadableNetworkUrl(url)"))
        assertTrue(enqueueController.contains("DownloadSafetyPolicy.safeDownloadFileName("))
        assertTrue(
            enqueueController.contains(
                "DownloadSafetyPolicy.requiresInsecureTransportConfirmation(browserManager().currentUrl(), url)"
            )
        )
        assertTrue(enqueueController.contains("private fun confirmDownloadIfNeeded("))
        assertTrue(enqueueController.contains("private fun showRiskyDownloadConfirmation"))
        assertTrue(enqueueController.contains("private fun showInsecureDownloadConfirmation"))
        assertTrue(enqueueController.contains("AlertDialog.Builder(activity)"))
        assertTrue(enqueueController.contains("R.string.title_confirm_app_download"))
        assertTrue(enqueueController.contains("R.string.dialog_confirm_app_download_message"))
        assertTrue(enqueueController.contains("R.string.title_confirm_insecure_download"))
        assertTrue(enqueueController.contains("R.string.dialog_confirm_insecure_download_message"))
        assertTrue(enqueueController.contains("R.string.action_download_anyway"))
        assertTrue(enqueueController.contains("private fun enqueueConfirmed("))
        assertTrue(strings.contains("title_confirm_app_download"))
        assertTrue(strings.contains("dialog_confirm_app_download_message"))
        assertTrue(strings.contains("title_confirm_insecure_download"))
        assertTrue(strings.contains("dialog_confirm_insecure_download_message"))
        assertTrue(strings.contains("action_download_anyway"))
        assertTrue(readme.contains("应用安装包类文件下载前会先确认"))
        assertTrue(readme.contains("HTTPS 页面触发 HTTP 明文下载前会先确认"))
        assertTrue(readme.contains("下载器只接受 HTTP/HTTPS 下载地址"))
        assertTrue(readme.contains("下载文件名会移除路径分隔符、控制字符和常见非法字符"))
    }

}
