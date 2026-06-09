package com.example.videobrowser.rules

import com.example.videobrowser.browser.ResourceType
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInRuleAssetContractTest {
    @Test
    fun gzwanmeiRules_blockThirdPartyAdScriptsOnlyOnTargetSite() {
        val engine = RuleEngine(assetRequestRules())

        listOf(
            "https://5936589oytqxc.t1e8s2.com/hm/oytqxc?x=pclcr7j115",
            "https://5936589ytymer.e1d8a4.com/hm/ytymer?x=ez5qyjtby115",
            "https://api.ttkxny.com/tj/tongjiv3.js?v=3.18"
        ).forEach { url ->
            assertTrue(
                "Expected $url to be blocked on m.gzwanmei.com.",
                engine.matchRequest(
                    url = url,
                    host = hostFromUrl(url),
                    pageHost = "m.gzwanmei.com",
                    resourceType = ResourceType.SCRIPT
                ).shouldBlock
            )
        }

        assertFalse(
            engine.matchRequest(
                url = "https://api.ttkxny.com/tj/tongjiv3.js?v=3.18",
                host = "api.ttkxny.com",
                pageHost = "example.com",
                resourceType = ResourceType.SCRIPT
            ).shouldBlock
        )
        assertFalse(
            engine.matchRequest(
                url = "https://m.gzwanmei.com/static/player/dplayer.html",
                host = "m.gzwanmei.com",
                pageHost = "m.gzwanmei.com",
                resourceType = ResourceType.DOCUMENT
            ).shouldBlock
        )
    }

    @Test
    fun gzwanmeiCssRules_hideGeneratedAdScaffoldWithoutRandomIds() {
        val engine = RuleEngine(emptyList(), assetCssRules())
        val selectors = engine.cssSelectorsFor("https://m.gzwanmei.com/vp/5/3-92.html")

        assertTrue(selectors.contains(".kpulx"))
        assertTrue(selectors.contains("lwyqvs"))
        assertTrue(selectors.contains("fpjzxz"))
        assertTrue(selectors.contains(".tdqqy"))
        assertFalse(selectors.any { selector -> selector.contains("pclcr7j115") })

        val unrelatedSelectors = engine.cssSelectorsFor("https://example.com/")
        assertFalse(unrelatedSelectors.contains(".kpulx"))
        assertFalse(unrelatedSelectors.contains("lwyqvs"))
        assertFalse(unrelatedSelectors.contains("fpjzxz"))
        assertFalse(unrelatedSelectors.contains(".tdqqy"))
    }

    private fun assetRequestRules(): List<Rule> {
        return loader().loadRequestRules().rules
    }

    private fun assetCssRules(): List<ElementRule> {
        return loader().loadCssRules().rules
    }

    private fun loader(): RuleFileLoader {
        return RuleFileLoader(
            openAsset = { path -> assetFile(path).inputStream() }
        )
    }

    private fun assetFile(path: String): File {
        return listOf(
            File("app/src/main/assets", path),
            File("src/main/assets", path)
        ).first { file -> file.isFile }
    }

    private fun hostFromUrl(url: String): String {
        return url.substringAfter("://").substringBefore("/")
    }
}
