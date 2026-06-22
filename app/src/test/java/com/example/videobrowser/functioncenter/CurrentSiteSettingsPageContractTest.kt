package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Current Site Settings Page Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrentSiteSettingsPageContractTest {
    /**
     * 测试函数 `currentSiteSettingsExposeSitePermissionDecisions`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `current Site Settings Expose Site Permission Decisions` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun currentSiteSettingsExposeSitePermissionDecisions() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/CurrentSiteSettingsPage.kt"
        ).readText()
        val permissionSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/CurrentSitePermissionSection.kt"
        ).readText()
        val formatter = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SitePermissionTextFormatter.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(page.contains("SitePermission.CAMERA"))
        assertTrue(page.contains("SitePermission.MICROPHONE"))
        assertTrue(page.contains("SitePermission.LOCATION"))
        assertTrue(page.contains("CurrentSitePermissionSection("))
        assertTrue(page.contains("sitePermissionSection.addRows(section, siteHost, hasSite)"))
        assertTrue(permissionSection.contains("SitePermissionDecision.entries"))
        assertTrue(permissionSection.contains("settingsManager.sitePermissionDecision(hostName, permission)"))
        assertTrue(permissionSection.contains("settingsManager.setSitePermissionDecision(hostName, permission, decision)"))
        assertTrue(permissionSection.contains("fun showSitePermissionDialog"))
        assertTrue(formatter.contains("R.string.site_permission_ask"))
        assertTrue(formatter.contains("R.string.site_permission_allowed"))
        assertTrue(formatter.contains("R.string.site_permission_blocked"))
        assertTrue(strings.contains("setting_site_permission_camera"))
        assertTrue(strings.contains("setting_site_permission_microphone"))
        assertTrue(strings.contains("setting_site_permission_location"))
        assertTrue(strings.contains("toast_site_permission_updated"))
    }

    @Test
    fun currentSiteSettingsDelegateFeatureSwitches() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/CurrentSiteSettingsPage.kt"
        ).readText()
        val featureSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/CurrentSiteFeatureSection.kt"
        ).readText()

        assertTrue(page.contains("CurrentSiteFeatureSection("))
        assertTrue(page.contains("siteFeatureSection.addRows(section, siteHost, hasSite)"))
        assertTrue(page.contains("siteFeatureSection.status("))
        assertTrue(featureSection.contains("settingsManager::setAdBlockDisabledForSite"))
        assertTrue(featureSection.contains("settingsManager::setSmartNoImageDisabledForSite"))
        assertTrue(featureSection.contains("settingsManager::setJsInjectionDisabledForSite"))
        assertTrue(featureSection.contains("settingsManager::setDomAdBlockDisabledForSite"))
        assertTrue(featureSection.contains("settingsManager::setVideoEnhancementDisabledForSite"))
        assertTrue(featureSection.contains("browserManager().reload()"))
        assertTrue(featureSection.contains("onChanged = injectPageFeatures"))
        assertTrue(featureSection.contains("R.string.toast_current_site_feature_enabled"))
        assertTrue(featureSection.contains("R.string.toast_current_site_feature_disabled"))
    }

}
