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

    fun clearRuleCache(filesDir: File): Boolean {
        val cacheDirectory = ruleCacheDirectory(filesDir)
        return !cacheDirectory.exists() || cacheDirectory.deleteRecursively()
    }

    fun ruleCacheDirectory(filesDir: File): File {
        return File(filesDir, RULE_CACHE_DIR)
    }

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
