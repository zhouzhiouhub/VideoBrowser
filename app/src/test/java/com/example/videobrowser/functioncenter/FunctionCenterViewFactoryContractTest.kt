package com.example.videobrowser.functioncenter

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FunctionCenterViewFactoryContractTest {
    @Test
    fun `view factory delegates page containers to page view factory`() {
        val viewFactory = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterViewFactory.kt"
        ).readText()
        val pageFactory = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPageViewFactory.kt"
        ).readText()

        assertTrue(viewFactory.contains("FunctionCenterPageViewFactory(activity, dp, surfaceFactory)"))
        assertTrue(viewFactory.contains("return pageFactory.createPage(title, onBack, buildContent)"))
        assertTrue(
            viewFactory.contains(
                "return pageFactory.createBottomSheetPage(title, onBack, onClose, buildContent)"
            )
        )

        assertTrue(pageFactory.contains("internal class FunctionCenterPageViewFactory"))
        assertTrue(pageFactory.contains("fun createPage("))
        assertTrue(pageFactory.contains("fun createBottomSheetPage("))
        assertTrue(pageFactory.contains("private fun scrollable(content: LinearLayout): ScrollView"))
        assertTrue(pageFactory.contains("private fun bottomSheetHeight(): Int"))
        assertTrue(pageFactory.contains("surfaceFactory.createToolbar(title, onBack)"))
        assertTrue(pageFactory.contains("surfaceFactory.createSheetToolbar(title, onBack, onClose)"))

        assertFalse(viewFactory.contains("FrameLayout("))
        assertFalse(viewFactory.contains("ScrollView("))
        assertFalse(viewFactory.contains("createSheetToolbar("))
        assertFalse(viewFactory.contains("displayMetrics.heightPixels"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
