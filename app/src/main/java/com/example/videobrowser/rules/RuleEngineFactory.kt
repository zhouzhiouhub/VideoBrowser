package com.example.videobrowser.rules

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
        logSkippedRules(
            skippedRules = requestResult.skippedRules + cssResult.skippedRules + domResult.skippedRules,
            logTag = logTag
        )
        val requestRules = BuiltInAdBlockRules.requestRules() + requestResult.rules
        val elementRules = cssResult.rules + domResult.rules
        return RuleEngine(
            rules = requestRules,
            elementRules = elementRules
        )
    }

    fun clearRuleCache(filesDir: File): Boolean {
        val cacheDirectory = File(filesDir, RULE_CACHE_DIR)
        return !cacheDirectory.exists() || cacheDirectory.deleteRecursively()
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
