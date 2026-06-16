package com.example.videobrowser.inject

/**
 * 测试阅读提示：
 * 这个测试文件验证“Script Loader Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScriptLoaderTest {
    @Test
    fun loadCommonScript_readsFromBundledScriptPath() {
        val requestedPaths = mutableListOf<String>()
        val loader = ScriptLoader { path ->
            requestedPaths += path
            ByteArrayInputStream("content from $path".toByteArray(Charsets.UTF_8))
        }

        val script = loader.loadCommonScript()

        assertEquals("content from ${ScriptLoader.COMMON_SCRIPT_ASSET}", script)
        assertEquals(listOf(ScriptLoader.COMMON_SCRIPT_ASSET), requestedPaths)
    }

    @Test
    fun loadScript_rejectsNonScriptAssetPaths() {
        assertInvalidPath("")
        assertInvalidPath("common.js")
        assertInvalidPath("scripts/../common.js")
        assertInvalidPath("scripts\\common.js")
        assertInvalidPath("scripts/common.css")
    }

    private fun assertInvalidPath(path: String) {
        val loader = ScriptLoader {
            ByteArrayInputStream("unused".toByteArray(Charsets.UTF_8))
        }

        val result = runCatching { loader.loadScript(path) }

        assertTrue("Expected invalid path: $path", result.isFailure)
    }
}
