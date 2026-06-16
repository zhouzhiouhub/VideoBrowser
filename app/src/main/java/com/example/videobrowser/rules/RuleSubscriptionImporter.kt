package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 RuleSubscriptionImporter 可以拆开理解为“Rule Subscription Importer”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.adguard.AdGuardRuleParser
import java.io.File
import java.util.Locale
import java.util.Properties

class RuleSubscriptionImporter(
    private val cacheDirectory: File,
    private val parser: AdGuardRuleParser = AdGuardRuleParser()
) {
    /**
     * 函数 `importText`：封装 `import Text` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param subscriptionId 参数类型为 `String`，表示函数执行 `subscriptionId` 相关逻辑时需要读取或处理的输入。
     * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun importText(subscriptionId: String, text: String): RuleSubscriptionImportResult {
        val normalizedId = normalizeSubscriptionId(subscriptionId)
        val parseResult = parser.parseSubscription(
            text = text,
            source = "subscription:$normalizedId"
        )
        cacheDirectory.mkdirs()

        val writes = listOf(
            RuleFileLoader.REQUEST_RULES_CACHE_FILE to parseResult.requestRuleLines,
            RuleFileLoader.CSS_RULES_CACHE_FILE to parseResult.cssRuleLines,
            RuleFileLoader.DOM_RULES_CACHE_FILE to emptyList<String>(),
            RuleFileLoader.SCRIPTLET_RULES_CACHE_FILE to parseResult.scriptletRuleLines,
            RuleFileLoader.REMOVE_PARAM_RULES_CACHE_FILE to parseResult.removeParamRuleLines
        )
        val tempFiles = writes.map { (fileName, lines) ->
            val tempFile = cacheDirectory.resolve("$fileName.tmp")
            tempFile.writeText(renderLines(lines), Charsets.UTF_8)
            fileName to tempFile
        }

        tempFiles.forEach { (fileName, tempFile) ->
            val targetFile = cacheDirectory.resolve(fileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }
            tempFile.renameTo(targetFile)
        }
        writeMetadata(normalizedId, parseResult)

        return RuleSubscriptionImportResult(
            updated = true,
            requestRuleCount = parseResult.requestRules.size,
            cssRuleCount = parseResult.elementRules.size,
            scriptletRuleCount = parseResult.scriptletRules.size,
            removeParamRuleCount = parseResult.removeParamRules.size,
            skippedRuleCount = parseResult.skippedRules.size
        )
    }

    /**
     * 函数 `update`：根据最新状态刷新 `update` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param subscriptionId 参数类型为 `String`，表示函数执行 `subscriptionId` 相关逻辑时需要读取或处理的输入。
     * @param fetchText 参数类型为 `() -> String`，表示函数执行 `fetchText` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun update(subscriptionId: String, fetchText: () -> String): RuleSubscriptionImportResult {
        return runCatching {
            importText(subscriptionId, fetchText())
        }.getOrElse { error ->
            RuleSubscriptionImportResult(
                updated = false,
                usedExistingCache = hasExistingCache(),
                errorMessage = error.message ?: error::class.java.simpleName
            )
        }
    }

    /**
     * 函数 `writeMetadata`：封装 `write Metadata` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param subscriptionId 参数类型为 `String`，表示函数执行 `subscriptionId` 相关逻辑时需要读取或处理的输入。
     * @param parseResult 参数类型为 `com.example.videobrowser.adguard.AdGuardParseResult`，表示函数执行 `parseResult` 相关逻辑时需要读取或处理的输入。
     */
    private fun writeMetadata(
        subscriptionId: String,
        parseResult: com.example.videobrowser.adguard.AdGuardParseResult
    ) {
        val properties = Properties().apply {
            setProperty(RuleFileLoader.METADATA_SOURCE_LABEL, "subscription:$subscriptionId")
            setProperty("request_rule_count", parseResult.requestRules.size.toString())
            setProperty("css_rule_count", parseResult.elementRules.size.toString())
            setProperty("scriptlet_rule_count", parseResult.scriptletRules.size.toString())
            setProperty("removeparam_rule_count", parseResult.removeParamRules.size.toString())
            setProperty("skipped_rule_count", parseResult.skippedRules.size.toString())
            setProperty("updated_at_epoch_ms", System.currentTimeMillis().toString())
        }
        cacheDirectory.resolve(RuleFileLoader.RULE_CACHE_METADATA_FILE)
            .outputStream()
            .use { output -> properties.store(output, "VideoBrowser rule subscription cache") }
    }

    /**
     * 函数 `hasExistingCache`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun hasExistingCache(): Boolean {
        return listOf(
            RuleFileLoader.REQUEST_RULES_CACHE_FILE,
            RuleFileLoader.CSS_RULES_CACHE_FILE,
            RuleFileLoader.DOM_RULES_CACHE_FILE,
            RuleFileLoader.SCRIPTLET_RULES_CACHE_FILE,
            RuleFileLoader.REMOVE_PARAM_RULES_CACHE_FILE
        ).any { fileName -> cacheDirectory.resolve(fileName).isFile }
    }

    /**
     * 函数 `renderLines`：封装 `render Lines` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param lines 参数类型为 `List<String>`，表示函数执行 `lines` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun renderLines(lines: List<String>): String {
        return if (lines.isEmpty()) {
            ""
        } else {
            lines.joinToString(separator = "\n", postfix = "\n")
        }
    }

    /**
     * 函数 `normalizeSubscriptionId`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param subscriptionId 参数类型为 `String`，表示函数执行 `subscriptionId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeSubscriptionId(subscriptionId: String): String {
        val normalized = subscriptionId
            .trim()
            .lowercase(Locale.US)
            .replace(Regex("\\s+"), "-")
            .filter { char -> char.isLetterOrDigit() || char == '-' || char == '_' || char == '.' }
            .trim('-', '_', '.')
        return normalized.ifBlank { "manual" }
    }
}

data class RuleSubscriptionImportResult(
    val updated: Boolean,
    val usedExistingCache: Boolean = false,
    val errorMessage: String? = null,
    val requestRuleCount: Int = 0,
    val cssRuleCount: Int = 0,
    val scriptletRuleCount: Int = 0,
    val removeParamRuleCount: Int = 0,
    val skippedRuleCount: Int = 0
)
