package com.example.videobrowser.settings

import com.example.videobrowser.rules.RuleEngineFactory
import java.io.File

class BrowserDefaultSettingsResetter(
    private val settingsManager: SettingsManager,
    private val filesDir: File
) {
    fun restoreDefaults(): Boolean {
        val settingsRestored = settingsManager.restoreDefaults()
        val ruleCacheCleared = RuleEngineFactory.clearRuleCache(filesDir)
        return settingsRestored && ruleCacheCleared
    }
}
