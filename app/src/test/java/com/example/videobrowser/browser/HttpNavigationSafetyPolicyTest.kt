package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Http Navigation Safety Policy Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpNavigationSafetyPolicyTest {
    /**
     * 测试函数 `requiresInsecureNavigationConfirmation_onlyForHttpsToHttpPageDowngrades`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `requires Insecure Navigation Confirmation only For Https To Http Page Downgrades` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun requiresInsecureNavigationConfirmation_onlyForHttpsToHttpPageDowngrades() {
        assertTrue(
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                pageUrl = "https://secure.example.com/page",
                targetUrl = "http://plain.example.com/"
            )
        )

        assertFalse(
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                pageUrl = "https://secure.example.com/page",
                targetUrl = "https://secure.example.com/next"
            )
        )
        assertFalse(
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                pageUrl = "http://plain.example.com/page",
                targetUrl = "http://plain.example.com/next"
            )
        )
        assertFalse(
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                pageUrl = null,
                targetUrl = "http://plain.example.com/"
            )
        )
    }

    /**
     * 测试函数 `requiresInsecureNavigationConfirmation_rejectsNonNetworkTargets`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `requires Insecure Navigation Confirmation rejects Non Network Targets` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun requiresInsecureNavigationConfirmation_rejectsNonNetworkTargets() {
        assertFalse(
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                pageUrl = "https://secure.example.com/page",
                targetUrl = "http:/missing-host"
            )
        )
        assertFalse(
            HttpNavigationSafetyPolicy.requiresInsecureNavigationConfirmation(
                pageUrl = "https://secure.example.com/page",
                targetUrl = "file:///sdcard/page.html"
            )
        )
    }
}
