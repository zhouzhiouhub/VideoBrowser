package com.example.videobrowser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Example Unit Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    /**
     * 测试函数 `addition_isCorrect`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `addition is Correct` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}