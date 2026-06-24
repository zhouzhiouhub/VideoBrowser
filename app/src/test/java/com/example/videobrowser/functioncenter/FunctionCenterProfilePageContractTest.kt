package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FunctionCenterProfilePageContractTest {
    @Test
    fun functionCenterPagesDelegatesProfileRenderingToProfilePage() {
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val profilePage = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterProfilePage.kt"
        ).readText()

        assertTrue(pages.contains("FunctionCenterProfilePage("))
        assertTrue(pages.contains("profilePage.show()"))
        assertFalse(pages.contains("FunctionCenterProfilePageLayout.blocks().forEach"))
        assertFalse(pages.contains("private fun addProfileShortcutSection"))
        assertFalse(pages.contains("private fun addProfileFeatureSection"))

        assertTrue(profilePage.contains("internal class FunctionCenterProfilePage"))
        assertTrue(profilePage.contains("FunctionCenterProfilePageLayout.blocks().forEach"))
        assertTrue(profilePage.contains("FunctionCenterProfilePageBlock.PROFILE_HEADER -> addProfileHeader(content)"))
        assertTrue(profilePage.contains("FunctionCenterProfilePageBlock.SHORTCUTS -> profileShortcutSection.add(content)"))
        assertTrue(profilePage.contains("FunctionCenterProfilePageBlock.FEATURES -> addProfileFeatureSection(content)"))
        assertTrue(profilePage.contains("browserSettingsPage.addExpandedBrowserSettings(parent)"))
        assertTrue(settingsPageExposesEnhancementsOnlyInExpandedSettings())
        assertTrue(profilePage.contains("browserSettingsPage.addProfileDataManagement(parent)"))
    }

    private fun settingsPageExposesEnhancementsOnlyInExpandedSettings(): Boolean {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt"
        ).readText()
        val functionStart = page.indexOf("fun addExpandedBrowserSettings(parent: LinearLayout)")
        if (functionStart < 0) {
            return false
        }
        val nextFunctionStart = page.indexOf("fun addExpandedDataManagement", functionStart)
        if (nextFunctionStart < 0) {
            return false
        }
        val functionBody = page.substring(functionStart, nextFunctionStart)
        return !functionBody.contains("addBrowserBasicsSection(parent)") &&
            functionBody.contains("addGlobalEnhancementSection(parent)")
    }

}
