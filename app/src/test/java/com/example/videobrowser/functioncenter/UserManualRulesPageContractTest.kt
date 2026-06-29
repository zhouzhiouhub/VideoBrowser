package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserManualRulesPageContractTest {
    @Test
    fun manualRulesPageCanStartSharedElementPicker() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/UserManualRulesPage.kt"
        ).readText()
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(page.contains("private val startElementPicker: () -> Unit"))
        assertTrue(page.contains("addActions(content, hasRules = rules.isNotEmpty())"))
        assertTrue(page.contains("activity.getString(R.string.action_pick_element)"))
        assertTrue(page.contains("activity.getString(R.string.action_pick_element_manual_rule_summary)"))
        assertTrue(page.contains("host.close()"))
        assertTrue(page.contains("startElementPicker()"))
        assertTrue(pages.contains("startElementPicker = startElementPicker"))
        assertTrue(strings.contains("name=\"action_pick_element_manual_rule_summary\""))

        assertFalse(page.contains("WebViewEnhancerScript"))
        assertFalse(page.contains("evaluateJavascript"))
        assertFalse(page.contains("requestElementBlock"))
        assertFalse(page.contains("VideoBrowserNativeBridge"))
    }
}
