package com.example.videobrowser.site

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SiteVideoCapabilityBrokerContractTest {
    @Test
    fun `site video capabilities are owned by shared broker module`() {
        val brokerScript = projectFile("src/main/assets/scripts/site_video_capability_broker.js").readText()
        val commonScript = projectFile("src/main/assets/scripts/common.js").readText()
        val scriptLoader = projectFile("src/main/java/com/example/videobrowser/inject/ScriptLoader.kt").readText()
        val commonAssetList = scriptLoader.substringAfter("val COMMON_SCRIPT_ASSETS = listOf(")

        assertTrue(brokerScript.contains("window.VideoBrowserSiteVideoCapabilityBroker = broker"))
        assertTrue(brokerScript.contains("broker.forVideo = broker.forVideo || function (video, action)"))
        assertTrue(brokerScript.contains("broker.has = broker.has || function (video, action)"))
        assertTrue(brokerScript.contains("broker.hasFromOptions = broker.hasFromOptions || function (options, video, action)"))
        assertTrue(brokerScript.contains("broker.invoke = broker.invoke || function (video, action, args)"))
        assertTrue(brokerScript.contains("broker.invokeFromOptions = broker.invokeFromOptions || function (options, video, action, args)"))
        assertTrue(brokerScript.contains("broker.unhandled = broker.unhandled || function ()"))
        assertTrue(brokerScript.contains("const adapters = window.VideoBrowserSiteAdapters || {}"))
        assertTrue(commonScript.contains("const siteVideoCapabilityBroker = window.VideoBrowserSiteVideoCapabilityBroker"))
        assertTrue(commonScript.contains("const hasSiteVideoCapability = siteVideoCapabilityBroker.has"))
        assertTrue(commonScript.contains("const invokeSiteVideoCapability = siteVideoCapabilityBroker.invoke"))
        assertTrue(scriptLoader.contains("SITE_VIDEO_CAPABILITY_BROKER_SCRIPT_ASSET"))
        assertTrue(
            commonAssetList.indexOf("SITE_VIDEO_CAPABILITY_BROKER_SCRIPT_ASSET") <
                commonAssetList.indexOf("COMMON_SCRIPT_ASSET")
        )
        assertFalse(commonScript.contains("function siteVideoCapabilitiesFor(video, action)"))
        assertFalse(commonScript.contains("function hasSiteVideoCapability(video, action)"))
        assertFalse(commonScript.contains("function invokeSiteVideoCapability(video, action, args)"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
