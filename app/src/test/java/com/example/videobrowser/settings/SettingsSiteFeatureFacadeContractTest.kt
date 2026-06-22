package com.example.videobrowser.settings

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsSiteFeatureFacadeContractTest {
    @Test
    fun settingsManagerDelegatesSiteFeatureSettingsToFacade() {
        val settingsManager = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsManager.kt"
        ).readText()
        val siteFeatureFacade = projectFile(
            "src/main/java/com/example/videobrowser/settings/SettingsSiteFeatureFacade.kt"
        ).readText()

        assertTrue(settingsManager.contains("SettingsSiteFeatureFacade("))
        assertTrue(settingsManager.contains("return siteFeatures.isAdBlockDisabledForSite(host)"))
        assertTrue(settingsManager.contains("return siteFeatures.setAdBlockDisabledForSite(host, disabled)"))
        assertTrue(settingsManager.contains("return siteFeatures.isUserWhitelistedSite(host)"))
        assertTrue(settingsManager.contains("siteFeatures.clearUserWhitelistedSites()"))
        assertFalse(settingsManager.contains("siteFeatureHosts.contains(KEY_SITE_"))
        assertFalse(settingsManager.contains("siteFeatureHosts.set(KEY_SITE_"))

        assertTrue(siteFeatureFacade.contains("KEY_SITE_AD_BLOCK_DISABLED_HOSTS"))
        assertTrue(siteFeatureFacade.contains("KEY_SITE_JS_INJECTION_DISABLED_HOSTS"))
        assertTrue(siteFeatureFacade.contains("KEY_SITE_DOM_AD_BLOCK_DISABLED_HOSTS"))
        assertTrue(siteFeatureFacade.contains("KEY_SITE_VIDEO_ENHANCEMENT_DISABLED_HOSTS"))
        assertTrue(siteFeatureFacade.contains("KEY_SITE_SMART_NO_IMAGE_DISABLED_HOSTS"))
        assertTrue(siteFeatureFacade.contains("KEY_USER_WHITELISTED_SITE_HOSTS"))
        assertTrue(siteFeatureFacade.contains("private fun contains(key: String, host: String?): Boolean"))
        assertTrue(siteFeatureFacade.contains("private fun set(key: String, host: String?, enabled: Boolean): Boolean"))
        assertTrue(siteFeatureFacade.contains("private fun hosts(key: String): Set<String>"))
    }

}
