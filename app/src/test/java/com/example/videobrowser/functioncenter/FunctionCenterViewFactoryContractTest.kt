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
        val aboutPage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/AboutPage.kt"
        ).readText()
        val profilePage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterProfilePage.kt"
        ).readText()
        val rootActionSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionSection.kt"
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
        assertTrue(pageHost.contains("internal val contentFactory = viewFactory.contentFactory"))
        assertTrue(pageHost.contains("internal val headerFactory = viewFactory.headerFactory"))
        assertTrue(pageHost.contains("internal val gridFactory = viewFactory.gridFactory"))

        assertFalse(pageHost.contains("fun addFunctionSection("))
        assertFalse(pageHost.contains("fun addInfoRow("))
        assertFalse(pageHost.contains("fun addFunctionMessage("))
        assertFalse(pageHost.contains("fun addProfileHeader("))
        assertFalse(pageHost.contains("fun addBenefitStrip("))
        assertFalse(pageHost.contains("fun addHistoryPreview("))
        assertFalse(pageHost.contains("fun addEmptyState("))
        assertFalse(pageHost.contains("fun addFunctionActionButton("))
        assertFalse(pageHost.contains("fun addActionGrid("))
        assertFalse(pageHost.contains("fun addSwitchRow("))
        assertFalse(pageHost.contains("fun addActionRow("))
        assertFalse(pageHost.contains("fun addDivider("))

        assertTrue(aboutPage.contains("host.contentFactory.addFunctionSection("))
        assertTrue(aboutPage.contains("host.contentFactory.addInfoRow("))
        assertTrue(profilePage.contains("host.headerFactory.addProfileHeader("))
        assertTrue(rootActionSection.contains("host.gridFactory.addActionGrid("))
    }

}
