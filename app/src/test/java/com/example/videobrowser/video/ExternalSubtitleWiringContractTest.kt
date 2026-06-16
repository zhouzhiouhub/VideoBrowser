package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“External Subtitle Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalSubtitleWiringContractTest {
    /**
     * 测试函数 `playableMediaItemCarriesSubtitleCandidates`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `playable Media Item Carries Subtitle Candidates` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playableMediaItemCarriesSubtitleCandidates() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayableMediaItem.kt"
        ).readText()

        assertTrue(source.contains("data class ExternalSubtitleCandidate"))
        assertTrue(source.contains("val subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList()"))
    }

    /**
     * 测试函数 `playerActivitySerializesSubtitleCandidatesAndAttachesThemToMediaItem`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `player Activity Serializes Subtitle Candidates And Attaches Them To Media Item` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playerActivitySerializesSubtitleCandidatesAndAttachesThemToMediaItem() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        assertTrue(source.contains("EXTRA_SUBTITLE_URIS"))
        assertTrue(source.contains("putStringArrayListExtra("))
        assertTrue(source.contains("subtitleCandidatesFromIntent()"))
        assertTrue(source.contains("setSubtitleConfigurations("))
        assertTrue(source.contains("MediaItem.SubtitleConfiguration.Builder"))
    }

    /**
     * 测试函数 `localDirectoryOpenPassesMatchedSubtitlesIntoNativePlayer`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `local Directory Open Passes Matched Subtitles Into Native Player` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun localDirectoryOpenPassesMatchedSubtitlesIntoNativePlayer() {
        val localFiles = projectFile(
            "src/main/java/com/example/videobrowser/localfiles/LocalFilesController.kt"
        ).readText()
        val pageActions = projectFile(
            "src/main/java/com/example/videobrowser/browser/PageActionsController.kt"
        ).readText().replace("\r\n", "\n")

        assertTrue(localFiles.contains("LocalSubtitleMatcher.findSubtitleCandidates"))
        assertTrue(localFiles.contains("onOpenDocumentUri("))
        assertTrue(localFiles.contains("subtitleCandidates"))
        assertTrue(pageActions.contains("subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList()"))
        assertTrue(pageActions.contains("mediaItem?.title ?: title,\n                subtitleCandidates"))
    }

    /**
     * 测试函数 `projectFile`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `project File` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
