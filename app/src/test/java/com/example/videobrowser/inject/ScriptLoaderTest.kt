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
    /**
     * 测试函数 `loadCommonScript_readsFromBundledScriptPath`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `load Common Script reads From Bundled Script Path` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `loadScript_rejectsNonScriptAssetPaths`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `load Script rejects Non Script Asset Paths` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun loadScript_rejectsNonScriptAssetPaths() {
        assertInvalidPath("")
        assertInvalidPath("common.js")
        assertInvalidPath("scripts/../common.js")
        assertInvalidPath("scripts\\common.js")
        assertInvalidPath("scripts/common.css")
    }

    /**
     * 测试函数 `assertInvalidPath`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `assert Invalid Path` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     */
    private fun assertInvalidPath(path: String) {
        val loader = ScriptLoader {
            ByteArrayInputStream("unused".toByteArray(Charsets.UTF_8))
        }

        val result = runCatching { loader.loadScript(path) }

        assertTrue("Expected invalid path: $path", result.isFailure)
    }
}
