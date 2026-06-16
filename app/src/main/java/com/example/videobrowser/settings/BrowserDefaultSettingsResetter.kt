package com.example.videobrowser.settings

/**
 * 初学者阅读提示：
 * 这个文件属于“设置模块”。
 * 文件名 BrowserDefaultSettingsResetter 可以拆开理解为“Browser Default Settings Resetter”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：封装浏览器设置、站点级开关、权限记录和恢复默认设置逻辑。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import com.example.videobrowser.browser.BrowserTabSessionRepository
import com.example.videobrowser.rules.RuleEngineFactory
import com.example.videobrowser.storage.SavedPageRepository
import java.io.File

class BrowserDefaultSettingsResetter(
    private val settingsManager: SettingsManager,
    private val savedPageRepository: SavedPageRepository,
    private val browserTabSessionRepository: BrowserTabSessionRepository,
    private val filesDir: File
) {
    fun restoreDefaults(): Boolean {
        val settingsRestored = settingsManager.restoreDefaults()
        savedPageRepository.clearAll()
        browserTabSessionRepository.clear()
        val ruleCacheCleared = RuleEngineFactory.clearRuleCache(filesDir)
        return settingsRestored && ruleCacheCleared
    }
}
