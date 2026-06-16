package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Safety Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadSafetyWiringContractTest {
    @Test
    fun downloadControllerConfirmsRiskyApplicationPackagesBeforeEnqueueing() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadController.kt"
        ).readText()
        val policy = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadSafetyPolicy.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(policy.contains("object DownloadSafetyPolicy"))
        assertTrue(policy.contains("DownloadCategory.APP"))
        assertTrue(policy.contains("requiresInsecureTransportConfirmation(pageUrl: String?, downloadUrl: String)"))
        assertTrue(policy.contains("schemeOf(pageUrl) == \"https\" && schemeOf(downloadUrl) == \"http\""))
        assertTrue(policy.contains("fun isDownloadableNetworkUrl(url: String): Boolean"))
        assertTrue(policy.contains("(scheme == \"http\" || scheme == \"https\")"))
        assertTrue(policy.contains("fun safeDownloadFileName(fileName: String): String"))
        assertTrue(policy.contains("invalidDownloadFileNameChars"))
        assertTrue(controller.contains("DownloadSafetyPolicy.requiresConfirmation(fileName, mimeType)"))
        assertTrue(controller.contains("DownloadSafetyPolicy.isDownloadableNetworkUrl(url)"))
        assertTrue(controller.contains("DownloadSafetyPolicy.safeDownloadFileName("))
        assertTrue(controller.contains("DownloadSafetyPolicy.requiresInsecureTransportConfirmation(browserManager().currentUrl(), url)"))
        assertTrue(controller.contains("private fun confirmDownloadIfNeeded("))
        assertTrue(controller.contains("private fun showRiskyDownloadConfirmation"))
        assertTrue(controller.contains("private fun showInsecureDownloadConfirmation"))
        assertTrue(controller.contains("AlertDialog.Builder(activity)"))
        assertTrue(controller.contains("R.string.title_confirm_app_download"))
        assertTrue(controller.contains("R.string.dialog_confirm_app_download_message"))
        assertTrue(controller.contains("R.string.title_confirm_insecure_download"))
        assertTrue(controller.contains("R.string.dialog_confirm_insecure_download_message"))
        assertTrue(controller.contains("R.string.action_download_anyway"))
        assertTrue(controller.contains("private fun enqueueConfirmed("))
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

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
