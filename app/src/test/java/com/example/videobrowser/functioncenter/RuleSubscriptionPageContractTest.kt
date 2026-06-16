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

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
