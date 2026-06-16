package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Video Diagnostics Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoDiagnosticsContractTest {
    /**
     * 测试函数 `nativeVideoControllersUseSharedDiagnosticLogTag`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `native Video Controllers Use Shared Diagnostic Log Tag` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun nativeVideoControllersUseSharedDiagnosticLogTag() {
        val fullscreenController = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoController.kt"
        ).readText()
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        listOf(fullscreenController, playerActivity).forEach { source ->
            assertTrue(source.contains("private const val VIDEO_LOG_TAG = \"VideoBrowserVideo\""))
            assertTrue(source.contains("Log.d(VIDEO_LOG_TAG"))
        }
    }

    /**
     * 测试函数 `fullscreenGestureOverlayDoesNotRenderSecondProgressBar`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fullscreen Gesture Overlay Does Not Render Second Progress Bar` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fullscreenGestureOverlayDoesNotRenderSecondProgressBar() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()

        assertFalse(source.contains("seekProgressTrack"))
        assertFalse(source.contains("seekProgressFill"))
        assertFalse(source.contains("setupSeekProgress()"))
        assertFalse(source.contains("updateSeekProgress("))
    }

    /**
     * 测试函数 `fullscreenGestureOverlayLetsBottomProgressTouchesPassThroughBeforeWakingControls`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fullscreen Gesture Overlay Lets Bottom Progress Touches Pass Through Before Waking Controls` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fullscreenGestureOverlayLetsBottomProgressTouchesPassThroughBeforeWakingControls() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val dispatchBody = functionBody(
            source,
            "override fun dispatchTouchEvent(event: MotionEvent): Boolean"
        )

        val bottomPassthroughIndex = dispatchBody.indexOf("if (touchStartedInBottomPassthrough)")
        val wakeControlsIndex = dispatchBody.indexOf("if (event.isWakeControlsAction())")

        assertTrue(bottomPassthroughIndex >= 0)
        assertTrue(wakeControlsIndex >= 0)
        assertTrue(bottomPassthroughIndex < wakeControlsIndex)
        assertTrue(dispatchBody.substring(bottomPassthroughIndex, wakeControlsIndex).contains("return false"))
    }

    /**
     * 测试函数 `nativePlayerAppliesDefaultEnhancementBeforePreparingAndCanRetryWithoutEffects`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `native Player Applies Default Enhancement Before Preparing And Can Retry Without Effects` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun nativePlayerAppliesDefaultEnhancementBeforePreparingAndCanRetryWithoutEffects() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        assertTrue(source.contains("NativeVideoEnhancement.defaultEffects()"))
        assertTrue(source.contains("exoPlayer.setVideoEffects(videoEffects)"))
        assertTrue(source.contains("retryPlaybackWithoutVideoEffects()"))
        assertTrue(
            source.indexOf("exoPlayer.setVideoEffects(videoEffects)") <
                source.indexOf("exoPlayer.prepare()")
        )
    }

    /**
     * 测试函数 `nativeVideoEnhancementUsesMedia3EffectModuleAndConservativeEffect`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `native Video Enhancement Uses Media3 Effect Module And Conservative Effect` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun nativeVideoEnhancementUsesMedia3EffectModuleAndConservativeEffect() {
        val versionCatalog = projectFile("gradle/libs.versions.toml").readText()
        val buildFile = projectFile("app/build.gradle.kts").readText()
        val enhancementFile = projectFileOrNull(
            "src/main/java/com/example/videobrowser/video/NativeVideoEnhancement.kt"
        )

        assertTrue(versionCatalog.contains("androidx-media3-effect"))
        assertTrue(buildFile.contains("libs.androidx.media3.effect"))
        assertTrue("Missing NativeVideoEnhancement.kt", enhancementFile?.exists() == true)

        val enhancementSource = enhancementFile!!.readText()
        assertTrue(enhancementSource.contains("fun defaultEffects()"))
        assertTrue(enhancementSource.contains("Contrast("))
        assertFalse(enhancementSource.contains("RIFE"))
        assertFalse(enhancementSource.contains("RealESRGAN"))
        assertFalse(enhancementSource.contains("RealCUGAN"))
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
            File(workingDirectory, "app/$path"),
            File(workingDirectory.parentFile, path)
        ).first { it.exists() }
    }

    /**
     * 测试函数 `projectFileOrNull`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `project File Or Null` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun projectFileOrNull(path: String): File? {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            File(workingDirectory.parentFile, path)
        ).firstOrNull { it.exists() }
    }

    /**
     * 测试函数 `functionBody`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `function Body` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param source 参数类型为 `String`，表示函数执行 `source` 相关逻辑时需要读取或处理的输入。
     * @param signature 参数类型为 `String`，表示函数执行 `signature` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun functionBody(source: String, signature: String): String {
        val start = source.indexOf(signature)
        assertTrue("Missing $signature", start >= 0)
        val braceStart = source.indexOf('{', start)
        assertTrue("Missing body for $signature", braceStart >= 0)

        var depth = 0
        for (index in braceStart until source.length) {
            when (source[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) {
                        return source.substring(braceStart + 1, index)
                    }
                }
            }
        }
        error("Unterminated function body for $signature")
    }
}
