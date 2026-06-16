package com.example.videobrowser.browser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Find In Page Controller Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FindInPageControllerTest {
    /**
     * 测试函数 `searchTrimsQueryAndStartsWebViewFind`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `search Trims Query And Starts Web View Find` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun searchTrimsQueryAndStartsWebViewFind() {
        val queries = mutableListOf<String>()
        val controller = FindInPageController(
            findAll = { query -> queries += query },
            findNext = {},
            clearMatches = {}
        )

        assertTrue(controller.search("  video  "))

        assertEquals(listOf("video"), queries)
        assertEquals("video", controller.currentQuery)
    }

    /**
     * 测试函数 `searchRejectsBlankQuery`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `search Rejects Blank Query` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun searchRejectsBlankQuery() {
        var findAllCalled = false
        val controller = FindInPageController(
            findAll = { findAllCalled = true },
            findNext = {},
            clearMatches = {}
        )

        assertFalse(controller.search("   "))

        assertFalse(findAllCalled)
        assertEquals(null, controller.currentQuery)
    }

    /**
     * 测试函数 `findNextAndPreviousRequireAnActiveQuery`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `find Next And Previous Require An Active Query` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun findNextAndPreviousRequireAnActiveQuery() {
        val directions = mutableListOf<Boolean>()
        val controller = FindInPageController(
            findAll = {},
            findNext = { forward -> directions += forward },
            clearMatches = {}
        )

        assertFalse(controller.findNext())
        assertFalse(controller.findPrevious())
        controller.search("clip")

        assertTrue(controller.findNext())
        assertTrue(controller.findPrevious())
        assertEquals(listOf(true, false), directions)
    }

    /**
     * 测试函数 `clearRemovesCurrentQueryAndClearsWebViewMatches`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `clear Removes Current Query And Clears Web View Matches` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun clearRemovesCurrentQueryAndClearsWebViewMatches() {
        var clearCount = 0
        val controller = FindInPageController(
            findAll = {},
            findNext = {},
            clearMatches = { clearCount++ }
        )
        controller.search("clip")

        controller.clear()

        assertEquals(null, controller.currentQuery)
        assertEquals(1, clearCount)
    }
}
