package com.example.videobrowser.functioncenter

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FunctionCenterToolbarButtonFactoryContractTest {
    @Test
    fun `toolbar button styling is centralized`() {
        val factory = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterToolbarButtonFactory.kt"
        ).readText()
        val surfaceFactory = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterSurfaceFactory.kt"
        ).readText()

        assertTrue(factory.contains("class FunctionCenterToolbarButtonFactory"))
        assertTrue(factory.contains("setImageResource(iconRes)"))
        assertTrue(factory.contains("R.drawable.bg_icon_button"))
        assertTrue(factory.contains("ViewCompat.setTooltipText(button, label)"))
        assertTrue(factory.contains("fun layoutParams()"))
        assertTrue(surfaceFactory.contains("FunctionCenterToolbarButtonFactory(activity, dp)"))
        assertTrue(surfaceFactory.contains("toolbarButtonFactory.createButton"))
        assertTrue(surfaceFactory.contains("toolbarButtonFactory.layoutParams()"))
        assertFalse(surfaceFactory.contains("ImageButton(activity).apply"))
        assertFalse(surfaceFactory.contains("setPadding(dp(16), dp(16), dp(16), dp(16))"))
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
