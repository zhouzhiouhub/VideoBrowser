package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoDiagnosticsContractTest {
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

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            File(workingDirectory.parentFile, path)
        ).first { it.exists() }
    }

    private fun projectFileOrNull(path: String): File? {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            File(workingDirectory.parentFile, path)
        ).firstOrNull { it.exists() }
    }

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
