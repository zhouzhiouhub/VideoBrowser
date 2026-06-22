package com.example.videobrowser.site

/**
 * 测试阅读提示：
 * 这个测试文件验证“Site Adapter Registry Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SiteAdapterRegistryTest {
    /**
     * 测试函数 `scriptFilesFor_matchesKnownDomainsWithSubdomainBoundary`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `script Files For matches Known Domains With Subdomain Boundary` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun scriptFilesFor_matchesKnownDomainsWithSubdomainBoundary() {
        val registry = SiteAdapterRegistry.default()

        assertEquals(
            listOf("scripts/youtube.js"),
            registry.scriptFilesFor("https://www.youtube.com/watch?v=1")
        )
        assertEquals(
            listOf(
                "scripts/bilibili_overlay_cleanup.js",
                "scripts/bilibili_browser_choice_cleanup.js",
                "scripts/bilibili.js"
            ),
            registry.scriptFilesFor("https://m.bilibili.com/video/BV1")
        )
        assertEquals(
            listOf("scripts/iqiyi.js"),
            registry.scriptFilesFor("https://iqiyi.com/")
        )
        assertEquals(
            listOf("scripts/tencent.js"),
            registry.scriptFilesFor("https://v.qq.com/x/cover/test.html")
        )
        assertEquals(
            listOf("scripts/youku.js"),
            registry.scriptFilesFor("https://www.youku.com/")
        )
    }

    /**
     * 测试函数 `scriptFilesFor_rejectsLookalikeDomains`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `script Files For rejects Lookalike Domains` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun scriptFilesFor_rejectsLookalikeDomains() {
        val registry = SiteAdapterRegistry.default()

        assertTrue(registry.scriptFilesFor("https://notyoutube.com/").isEmpty())
        assertTrue(registry.scriptFilesFor("https://youtube.com.example.com/").isEmpty())
        assertTrue(registry.scriptFilesFor("https://qq.com/").isEmpty())
        assertTrue(registry.scriptFilesFor("about:blank").isEmpty())
        assertTrue(registry.scriptFilesFor(null).isEmpty())
    }

    /**
     * 测试函数 `matchingAdapters_acceptsUrlWithoutScheme`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `matching Adapters accepts Url Without Scheme` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun matchingAdapters_acceptsUrlWithoutScheme() {
        val registry = SiteAdapterRegistry.default()

        assertEquals(
            listOf("scripts/youtube.js"),
            registry.scriptFilesFor("m.youtube.com/watch?v=1")
        )
    }
}
