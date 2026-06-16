package com.example.videobrowser.adblock

/**
 * 测试阅读提示：
 * 这个测试文件验证“Synthetic Response Registry Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyntheticResponseRegistryTest {
    private val registry = SyntheticResponseRegistry()

    /**
     * 测试函数 `get_returnsOnlyBuiltInNoopResources`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `get returns Only Built In Noop Resources` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun get_returnsOnlyBuiltInNoopResources() {
        assertEquals("application/javascript", registry.get("noopjs")?.mimeType)
        assertEquals("text/css", registry.get("noopcss")?.mimeType)
        assertEquals("text/plain", registry.get("nooptext")?.mimeType)

        assertNull(registry.get("https://evil.test/payload.js"))
        assertNull(registry.get("unknown"))
        assertNull(registry.get(""))
    }

    /**
     * 测试函数 `get_normalizesResourceName`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `get normalizes Resource Name` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun get_normalizesResourceName() {
        assertEquals("noopjs", registry.get(" NoOpJs ")?.name)
    }

    /**
     * 测试函数 `get_usesSafeUtf8BodiesAndOkStatus`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `get uses Safe Utf8 Bodies And Ok Status` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun get_usesSafeUtf8BodiesAndOkStatus() {
        val noopJs = requireNotNull(registry.get("noopjs"))
        val noopCss = requireNotNull(registry.get("noopcss"))
        val noopText = requireNotNull(registry.get("nooptext"))

        assertEquals(200, noopJs.statusCode)
        assertEquals("OK", noopJs.reasonPhrase)
        assertEquals("utf-8", noopJs.encoding)
        assertArrayEquals("/* noop */\n".toByteArray(Charsets.UTF_8), noopJs.body)
        assertArrayEquals("/* noop */\n".toByteArray(Charsets.UTF_8), noopCss.body)
        assertArrayEquals(ByteArray(0), noopText.body)
    }
}
