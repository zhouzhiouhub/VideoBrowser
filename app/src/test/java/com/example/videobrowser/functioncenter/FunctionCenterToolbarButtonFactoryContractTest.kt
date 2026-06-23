package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertEquals
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
        assertTrue(surfaceFactory.contains("private fun createToolbarTitle(title: String): TextView"))
        assertTrue(surfaceFactory.contains("private fun LinearLayout.addToolbarTitle(title: String)"))
        assertEquals(2, Regex("addToolbarTitle\\(title\\)").findAll(surfaceFactory).count())
        assertEquals(1, Regex("textSize = 18f").findAll(surfaceFactory).count())
        assertFalse(surfaceFactory.contains("ImageButton(activity).apply"))
        assertFalse(surfaceFactory.contains("setPadding(dp(16), dp(16), dp(16), dp(16))"))
    }

}
