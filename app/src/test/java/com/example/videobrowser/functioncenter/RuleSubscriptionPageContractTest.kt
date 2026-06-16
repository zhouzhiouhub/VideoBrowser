package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Rule Subscription Page Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleSubscriptionPageContractTest {
    /**
     * 测试函数 `ruleSubscriptionPageUsesHardenedFetcher`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `rule Subscription Page Uses Hardened Fetcher` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun ruleSubscriptionPageUsesHardenedFetcher() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/RuleSubscriptionPage.kt"
        ).readText()
        val fetcher = projectFile(
            "src/main/java/com/example/videobrowser/rules/RuleSubscriptionFetcher.kt"
        ).readText()
        val readme = projectFile("README.md").readText()

        assertTrue(page.contains("private val ruleSubscriptionFetcher = RuleSubscriptionFetcher()"))
        assertTrue(page.contains("RuleSubscriptionFetcher.subscriptionIdForUrl(url)"))
        assertTrue(page.contains("ruleSubscriptionFetcher.fetchText(url)"))
        assertTrue(fetcher.contains("scheme != \"http\" && scheme != \"https\""))
        assertTrue(fetcher.contains("uri.host.isNullOrBlank()"))
        assertTrue(fetcher.contains("readTextWithByteLimit"))
        assertTrue(readme.contains("规则订阅 URL 仅接受带主机名的 HTTP/HTTPS 地址"))
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
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
