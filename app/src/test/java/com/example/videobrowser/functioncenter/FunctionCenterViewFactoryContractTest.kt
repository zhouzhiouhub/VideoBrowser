package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile

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
        val pageHost = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPageHost.kt"
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
        assertFalse(viewFactory.contains("fun addFunctionSection("))
        assertFalse(viewFactory.contains("fun addActionRow("))
        assertFalse(viewFactory.contains("fun addSwitchRow("))
        assertFalse(viewFactory.contains("fun addActionGrid("))

        assertTrue(viewFactory.contains("internal val contentFactory = FunctionCenterContentFactory("))
        assertTrue(viewFactory.contains("internal val headerFactory = FunctionCenterHeaderFactory("))
        assertTrue(viewFactory.contains("internal val gridFactory = FunctionCenterGridFactory(activity, dp)"))
        assertTrue(viewFactory.contains("private val rowFactory = FunctionCenterRowFactory(activity, dp, surfaceFactory)"))
        assertTrue(pageHost.contains("private val contentFactory = viewFactory.contentFactory"))
        assertTrue(pageHost.contains("private val headerFactory = viewFactory.headerFactory"))
        assertTrue(pageHost.contains("private val gridFactory = viewFactory.gridFactory"))
        assertTrue(pageHost.contains("contentFactory.addFunctionSection(parent, title, buildContent)"))
        assertTrue(pageHost.contains("headerFactory.addProfileHeader(parent, title, summary, onClick)"))
        assertTrue(pageHost.contains("gridFactory.addActionGrid(parent, actions)"))
    }

}
