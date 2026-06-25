package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FunctionCenterPageContainerControllerContractTest {
    @Test
    fun functionCenterControllerDelegatesPageContainerStateToContainerController() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterController.kt"
        ).readText()
        val containerController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPageContainerController.kt"
        ).readText()

        assertTrue(controller.contains("FunctionCenterPageContainerController(rootView)"))
        assertTrue(controller.contains("FunctionCenterPageHistory<FunctionCenterPageState>()"))
        assertTrue(controller.contains("pageContainer.attach("))
        assertTrue(controller.contains("onSaveCurrentPage = pageHistory::push"))
        assertTrue(controller.contains("pageContainer.restore(previousPage)"))
        assertTrue(controller.contains("pageContainer.close()"))
        assertTrue(controller.contains("pageContainer.invokeBackAction()"))

        assertTrue(containerController.contains("internal data class FunctionCenterPageState"))
        assertTrue(containerController.contains("internal class FunctionCenterPageContainerController"))
        assertTrue(containerController.contains("private var page: View? = null"))
        assertTrue(containerController.contains("private var backAction: (() -> Unit)? = null"))
        assertTrue(containerController.contains("fun attach("))
        assertTrue(containerController.contains("fun restore(pageState: FunctionCenterPageState): Boolean"))
        assertTrue(containerController.contains("fun close(): Boolean"))
        assertTrue(containerController.contains("fun invokeBackAction(): Boolean"))
        assertTrue(containerController.contains("ConstraintLayout.LayoutParams("))

        assertFalse(controller.contains("private var page: View?"))
        assertFalse(controller.contains("private var backAction"))
        assertFalse(controller.contains("private data class PageState"))
        assertFalse(controller.contains("private fun restorePage"))
        assertFalse(controller.contains("private fun addPageToContainer"))
        assertFalse(controller.contains("ConstraintLayout.LayoutParams("))
    }

    @Test
    fun functionCenterControllerSharesPageCreationBeforeAttach() {
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterController.kt"
        ).readText()

        assertTrue(controller.contains("private fun attachStandardPage("))
        assertTrue(controller.contains("private fun attachStandardPageWithFooter("))
        assertTrue(controller.contains("private fun attachBottomSheetPage("))
        assertTrue(controller.contains("attachStandardPage(title, onBack, saveCurrentPage = true, buildContent)"))
        assertTrue(controller.contains("attachStandardPage(title, onBack, saveCurrentPage = false, buildContent)"))
        assertTrue(controller.contains("fun showPageWithFooter("))
        assertTrue(controller.contains("fun replacePageWithFooter("))
        assertTrue(controller.contains("viewFactory.createPageWithFooter("))
        assertEquals(1, Regex("viewFactory\\.createPage\\(").findAll(controller).count())
        assertEquals(1, Regex("viewFactory\\.createPageWithFooter\\(").findAll(controller).count())
        assertEquals(1, Regex("viewFactory\\.createBottomSheetPage\\(").findAll(controller).count())
    }

}
