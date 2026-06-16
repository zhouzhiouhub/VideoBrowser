package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“About Page Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AboutPageContractTest {
    /**
     * 测试函数 `aboutPageOnlyDisplaysVersionNumber`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `about Page Only Displays Version Number` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun aboutPageOnlyDisplaysVersionNumber() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/AboutPage.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(source.contains("R.string.about_version"))
        assertFalse(source.contains("R.string.about_app_name"))
        assertFalse(source.contains("R.string.about_git_commit_count"))
        assertFalse(source.contains("BuildConfig.GIT_COMMIT_COUNT"))
        assertFalse(strings.contains("name=\"about_app_name\""))
        assertFalse(strings.contains("name=\"about_git_commit_count\""))
        assertFalse(strings.contains("name=\"about_git_commit_count_summary\""))
        assertTrue(strings.contains("name=\"action_about_summary\">查看版本号</string>"))
    }

    /**
     * 测试函数 `projectFile`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `project File` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
