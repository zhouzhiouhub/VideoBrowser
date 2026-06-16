package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserTabSessionRepository 可以拆开理解为“Browser Tab Session Repository”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import com.example.videobrowser.storage.PreferenceStore
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale

data class BrowserTabSession(
    val tabs: List<BrowserTab>,
    val activeTabId: Long
)

class BrowserTabSessionRepository(
    private val preferenceStore: PreferenceStore
) {
    fun save(tabs: List<BrowserTab>, activeTabId: Long) {
        val sessionTabs = normalizeTabs(tabs)
        if (sessionTabs.isEmpty()) {
            clear()
            return
        }

        val activeId = activeTabId
            .takeIf { tabId -> sessionTabs.any { tab -> tab.id == tabId } }
            ?: sessionTabs.first().id
        preferenceStore.putString(KEY_STANDARD_TAB_SESSION, renderSession(activeId, sessionTabs))
    }

    fun restore(): BrowserTabSession? {
        val rawValue = preferenceStore.getString(KEY_STANDARD_TAB_SESSION, null) ?: return null
        return runCatching {
            val lines = rawValue.lineSequence().filter { line -> line.isNotBlank() }.toList()
            val activeIdFromStorage = lines.firstOrNull()?.toLongOrNull() ?: return null
            val tabs = normalizeTabs(lines.drop(1).mapNotNull(::parseTabLine))
            if (tabs.isEmpty()) {
                return null
            }
            val activeId = activeIdFromStorage
                .takeIf { tabId -> tabs.any { tab -> tab.id == tabId } }
                ?: tabs.first().id
            BrowserTabSession(tabs = tabs, activeTabId = activeId)
        }.getOrNull()
    }

    fun clear() {
        preferenceStore.remove(KEY_STANDARD_TAB_SESSION)
    }

    private fun normalizeTabs(tabs: List<BrowserTab>): List<BrowserTab> {
        return tabs
            .mapNotNull { tab -> normalizeTab(tab) }
            .distinctBy { tab -> tab.id }
            .take(MAX_SESSION_TABS)
    }

    private fun normalizeTab(tab: BrowserTab): BrowserTab? {
        if (tab.id <= 0L) {
            return null
        }
        val url = normalizeRestorableWebUrl(tab.url) ?: return null
        return tab.copy(
            url = url,
            title = tab.title.trim().take(MAX_TITLE_LENGTH)
        )
    }

    private fun normalizeRestorableWebUrl(url: String?): String? {
        val normalizedUrl = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val uri = runCatching { URI(normalizedUrl) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return null
        if (scheme != "http" && scheme != "https") {
            return null
        }
        if (uri.host.isNullOrBlank()) {
            return null
        }
        return normalizedUrl
    }

    private fun renderSession(activeTabId: Long, tabs: List<BrowserTab>): String {
        return buildString {
            append(activeTabId).append('\n')
            tabs.forEach { tab ->
                append(tab.id)
                    .append('\t')
                    .append(tab.createdAtMillis)
                    .append('\t')
                    .append(encode(tab.url.orEmpty()))
                    .append('\t')
                    .append(encode(tab.title))
                    .append('\n')
            }
        }
    }

    private fun parseTabLine(line: String): BrowserTab? {
        val parts = line.split('\t')
        if (parts.size != 4) {
            return null
        }
        val id = parts[0].toLongOrNull()?.takeIf { it > 0L } ?: return null
        val createdAtMillis = parts[1].toLongOrNull() ?: System.currentTimeMillis()
        val url = decode(parts[2])?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val title = decode(parts[3])?.trim().orEmpty()
        return BrowserTab(
            id = id,
            url = url,
            title = title.take(MAX_TITLE_LENGTH),
            createdAtMillis = createdAtMillis
        )
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, CHARSET_NAME)
    }

    private fun decode(value: String): String? {
        return runCatching {
            URLDecoder.decode(value, CHARSET_NAME)
        }.getOrNull()
    }

    companion object {
        const val KEY_STANDARD_TAB_SESSION = "standard_tab_session"
        private const val CHARSET_NAME = "UTF-8"
        private const val MAX_SESSION_TABS = 50
        private const val MAX_TITLE_LENGTH = 200
    }
}
