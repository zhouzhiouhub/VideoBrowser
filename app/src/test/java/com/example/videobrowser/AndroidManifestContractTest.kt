package com.example.videobrowser

/**
 * 测试阅读提示：
 * 这个测试文件验证“Android Manifest Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class AndroidManifestContractTest {
    /**
     * 测试函数 `mainActivityCanHandleExternalWebLinks`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Can Handle External Web Links` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityCanHandleExternalWebLinks() {
        val activity = manifest()
            .elements("activity")
            .first { it.androidAttribute("name") == ".MainActivity" }

        val webLinkFilter = activity
            .elements("intent-filter")
            .firstOrNull { filter ->
                filter.hasAction("android.intent.action.VIEW") &&
                    filter.hasCategory("android.intent.category.DEFAULT") &&
                    filter.hasCategory("android.intent.category.BROWSABLE")
            }

        assertTrue(
            "MainActivity should expose a VIEW/DEFAULT/BROWSABLE intent-filter for web links",
            webLinkFilter != null
        )
        val schemes = webLinkFilter!!.elements("data")
            .map { data -> data.androidAttribute("scheme") }
            .toSet()
        assertTrue("MainActivity should handle http links", "http" in schemes)
        assertTrue("MainActivity should handle https links", "https" in schemes)
    }

    /**
     * 测试函数 `mainActivityLoadsExternalWebLinkIntents`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `main Activity Loads External Web Link Intents` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun mainActivityLoadsExternalWebLinkIntents() {
        val mainActivity = projectFile(
            "src/main/java/com/example/videobrowser/MainActivity.kt"
        ).readText()

        assertTrue(mainActivity.contains("handleLaunchIntent(intent)"))
        assertTrue(mainActivity.contains("override fun onNewIntent(intent: Intent)"))
        assertTrue(mainActivity.contains("Intent.ACTION_VIEW"))
        assertTrue(mainActivity.contains("intent.dataString"))
    }

    /**
     * 测试函数 `browserLocalDataIsExcludedFromSystemBackup`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `browser Local Data Is Excluded From System Backup` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun browserLocalDataIsExcludedFromSystemBackup() {
        val application = manifest().elements("application").first()
        val backupRules = projectFile("src/main/res/xml/backup_rules.xml").readText()
        val dataExtractionRules = projectFile("src/main/res/xml/data_extraction_rules.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(application.androidAttribute("allowBackup") == "false")
        assertTrue(backupRules.contains("<exclude domain=\"file\" path=\".\""))
        assertTrue(backupRules.contains("<exclude domain=\"database\" path=\".\""))
        assertTrue(backupRules.contains("<exclude domain=\"sharedpref\" path=\".\""))
        assertTrue(dataExtractionRules.contains("<cloud-backup>"))
        assertTrue(dataExtractionRules.contains("<device-transfer>"))
        assertTrue(dataExtractionRules.contains("<exclude domain=\"file\" path=\".\""))
        assertTrue(dataExtractionRules.contains("<exclude domain=\"database\" path=\".\""))
        assertTrue(dataExtractionRules.contains("<exclude domain=\"sharedpref\" path=\".\""))
        assertTrue(readme.contains("避免系统自动备份本地浏览数据"))
    }

    /**
     * 测试函数 `manifest`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `manifest` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun manifest(): Element {
        return DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder()
            .parse(projectFile("src/main/AndroidManifest.xml"))
            .documentElement
    }

    /**
     * 测试函数 `hasAction`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `has Action` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun Element.hasAction(name: String): Boolean {
        return elements("action").any { action -> action.androidAttribute("name") == name }
    }

    /**
     * 测试函数 `hasCategory`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `has Category` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun Element.hasCategory(name: String): Boolean {
        return elements("category").any { category -> category.androidAttribute("name") == name }
    }

    /**
     * 测试函数 `elements`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `elements` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tagName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun Element.elements(tagName: String): List<Element> {
        val nodes = getElementsByTagName(tagName)
        return List(nodes.length) { index -> nodes.item(index) }
            .filterIsInstance<Element>()
    }

    /**
     * 测试函数 `androidAttribute`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `android Attribute` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param name 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun Element.androidAttribute(name: String): String {
        return getAttributeNS(ANDROID_NAMESPACE, name)
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

    private companion object {
        private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
