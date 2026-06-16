package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 RuleEngineFactory 可以拆开理解为“Rule Engine Factory”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import android.content.res.AssetManager
import android.util.Log
import com.example.videobrowser.adblock.BuiltInAdBlockRules
import java.io.File

object RuleEngineFactory {
    /**
     * 函数 `create`：创建 `create` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param assets 参数类型为 `AssetManager`，表示函数执行 `assets` 相关逻辑时需要读取或处理的输入。
     * @param filesDir 参数类型为 `File`，表示函数执行 `filesDir` 相关逻辑时需要读取或处理的输入。
     * @param logTag 参数类型为 `String`，表示函数执行 `logTag` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun create(
        assets: AssetManager,
        filesDir: File,
        logTag: String = RULE_LOG_TAG
    ): RuleEngine {
        val loader = RuleFileLoader.fromAssets(
            assets = assets,
            cacheDirectory = File(filesDir, RULE_CACHE_DIR)
        )
        val requestResult = loader.loadRequestRules()
        val cssResult = loader.loadCssRules()
        val domResult = loader.loadDomRules()
        val scriptletResult = loader.loadScriptletRules()
        val removeParamResult = loader.loadRemoveParamRules()
        logSkippedRules(
            skippedRules = requestResult.skippedRules +
                cssResult.skippedRules +
                domResult.skippedRules +
                scriptletResult.skippedRules +
                removeParamResult.skippedRules,
            logTag = logTag
        )
        val requestRules = BuiltInAdBlockRules.requestRules() + requestResult.rules
        val elementRules = cssResult.rules + domResult.rules
        return RuleEngine(
            rules = requestRules,
            elementRules = elementRules,
            scriptletRules = scriptletResult.rules,
            removeParamRules = removeParamResult.rules
        )
    }

    /**
     * 函数 `clearRuleCache`：封装 `clear Rule Cache` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param filesDir 参数类型为 `File`，表示函数执行 `filesDir` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun clearRuleCache(filesDir: File): Boolean {
        val cacheDirectory = ruleCacheDirectory(filesDir)
        return !cacheDirectory.exists() || cacheDirectory.deleteRecursively()
    }

    /**
     * 函数 `ruleCacheDirectory`：封装 `rule Cache Directory` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param filesDir 参数类型为 `File`，表示函数执行 `filesDir` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun ruleCacheDirectory(filesDir: File): File {
        return File(filesDir, RULE_CACHE_DIR)
    }

    /**
     * 函数 `logSkippedRules`：封装 `log Skipped Rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param skippedRules 参数类型为 `List<SkippedRule>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @param logTag 参数类型为 `String`，表示函数执行 `logTag` 相关逻辑时需要读取或处理的输入。
     */
    private fun logSkippedRules(skippedRules: List<SkippedRule>, logTag: String) {
        skippedRules.forEach { skippedRule ->
            Log.w(
                logTag,
                "Skipped ${skippedRule.source}:${skippedRule.lineNumber} " +
                    "(${skippedRule.reason}): ${skippedRule.text}"
            )
        }
    }

    private const val RULE_CACHE_DIR = "rules"
    private const val RULE_LOG_TAG = "VideoBrowserRules"
}
