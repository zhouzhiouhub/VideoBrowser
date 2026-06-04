package com.example.videobrowser.settings

import com.example.videobrowser.rules.RuleEngineFactory
import com.example.videobrowser.storage.SavedPageRepository
import java.io.File

class BrowserDefaultSettingsResetter(
    private val settingsManager: SettingsManager,
    private val savedPageRepository: SavedPageRepository,
    private val filesDir: File
) {
    fun restoreDefaults(): Boolean {
        val settingsRestored = settingsManager.restoreDefaults()
        savedPageRepository.clearAll()
        val ruleCacheCleared = RuleEngineFactory.clearRuleCache(filesDir)
        return settingsRestored && ruleCacheCleared
    }
}
