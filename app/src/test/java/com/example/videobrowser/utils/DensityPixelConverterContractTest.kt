package com.example.videobrowser.utils

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DensityPixelConverterContractTest {
    @Test
    fun `activity and fullscreen overlay share density conversion`() {
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/MainActivity.kt"),
            projectFile("src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("DensityPixelConverter."))
            assertFalse(source.contains("resources.displayMetrics.density"))
        }
    }

}
