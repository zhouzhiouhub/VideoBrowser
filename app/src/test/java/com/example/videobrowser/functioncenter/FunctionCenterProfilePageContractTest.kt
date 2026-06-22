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
        assertTrue(profilePage.contains("browserSettingsPage.addProfileDataManagement(parent)"))
    }

}
