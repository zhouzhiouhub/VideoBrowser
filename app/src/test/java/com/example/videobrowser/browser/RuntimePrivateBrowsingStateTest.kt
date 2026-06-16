package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Runtime Private Browsing State Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimePrivateBrowsingStateTest {
    /**
     * 测试函数 `startsInStandardMode`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `starts In Standard Mode` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun startsInStandardMode() {
        val state = RuntimePrivateBrowsingState()

        assertEquals(BrowserMode.STANDARD, state.mode)
        assertFalse(state.isPrivate)
    }

    /**
     * 测试函数 `enterAndExitPrivateMode_areRuntimeOnly`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `enter And Exit Private Mode are Runtime Only` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun enterAndExitPrivateMode_areRuntimeOnly() {
        var cleanupCalls = 0
        val state = RuntimePrivateBrowsingState(onPrivateCleanup = { cleanupCalls++ })

        assertTrue(state.enterPrivate())
        assertEquals(BrowserMode.PRIVATE, state.mode)
        assertTrue(state.isPrivate)

        assertTrue(state.exitPrivate())
        assertEquals(BrowserMode.STANDARD, state.mode)
        assertFalse(state.isPrivate)
        assertEquals(1, cleanupCalls)
    }

    /**
     * 测试函数 `repeatedTransitions_doNotRepeatCleanup`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `repeated Transitions do Not Repeat Cleanup` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun repeatedTransitions_doNotRepeatCleanup() {
        var cleanupCalls = 0
        val state = RuntimePrivateBrowsingState(onPrivateCleanup = { cleanupCalls++ })

        assertFalse(state.exitPrivate())
        assertTrue(state.enterPrivate())
        assertFalse(state.enterPrivate())
        assertTrue(state.exitPrivate())
        assertFalse(state.exitPrivate())

        assertEquals(BrowserMode.STANDARD, state.mode)
        assertEquals(1, cleanupCalls)
    }
}
