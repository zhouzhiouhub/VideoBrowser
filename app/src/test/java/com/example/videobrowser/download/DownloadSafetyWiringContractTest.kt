package com.example.videobrowser.download

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
        assertTrue(controller.contains("DownloadSafetyPolicy.requiresConfirmation(fileName, mimeType)"))
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
