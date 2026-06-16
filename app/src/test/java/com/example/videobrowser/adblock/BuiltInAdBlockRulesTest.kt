package com.example.videobrowser.adblock

/**
 * 测试阅读提示：
 * 这个测试文件验证“Built In Ad Block Rules Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInAdBlockRulesTest {
    /**
     * 测试函数 `matches_blocksKnownAdHosts`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `matches blocks Known Ad Hosts` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun matches_blocksKnownAdHosts() {
        assertTrue(
            BuiltInAdBlockRules.matches(
                url = "https://stats.g.doubleclick.net/pagead/viewthroughconversion.js",
                host = "stats.g.doubleclick.net"
            )
        )
        assertTrue(
            BuiltInAdBlockRules.matches(
                url = "https://cdn.adservice.google.com/script.js",
                host = "cdn.adservice.google.com"
            )
        )
    }

    /**
     * 测试函数 `matches_blocksKnownAdUrlKeywords`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `matches blocks Known Ad Url Keywords` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun matches_blocksKnownAdUrlKeywords() {
        assertTrue(
            BuiltInAdBlockRules.matches(
                url = "https://example.com/static/pagead/banner.js",
                host = "example.com"
            )
        )
        assertTrue(
            BuiltInAdBlockRules.matches(
                url = "https://video.example.com/media/preroll/clip.m3u8",
                host = "video.example.com"
            )
        )
    }

    /**
     * 测试函数 `matches_allowsNormalResources`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `matches allows Normal Resources` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun matches_allowsNormalResources() {
        assertFalse(
            BuiltInAdBlockRules.matches(
                url = "https://example.com/assets/app.js",
                host = "example.com"
            )
        )
        assertFalse(
            BuiltInAdBlockRules.matches(
                url = "https://cdn.example.com/images/poster.jpg",
                host = "cdn.example.com"
            )
        )
    }

    /**
     * 测试函数 `matches_doesNotTreatHostSubstringAsDomainMatch`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `matches does Not Treat Host Substring As Domain Match` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun matches_doesNotTreatHostSubstringAsDomainMatch() {
        assertFalse(
            BuiltInAdBlockRules.matches(
                url = "https://notdoubleclick.net/assets/app.js",
                host = "notdoubleclick.net"
            )
        )
    }
}
