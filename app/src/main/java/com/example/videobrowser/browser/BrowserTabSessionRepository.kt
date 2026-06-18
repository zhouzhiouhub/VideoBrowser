package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserTabSessionRepository 可以拆开理解为“Browser Tab Session Repository”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.utils.Utf8UrlCodec
import com.example.videobrowser.utils.WebUrlNormalizer

data class BrowserTabSession(
    val tabs: List<BrowserTab>,
    val activeTabId: Long
)

class BrowserTabSessionRepository(
    private val preferenceStore: PreferenceStore
) {
    /**
     * 函数 `save`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabs 参数类型为 `List<BrowserTab>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @param activeTabId 参数类型为 `Long`，表示函数执行 `activeTabId` 相关逻辑时需要读取或处理的输入。
     */
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

    /**
     * 函数 `restore`：封装 `restore` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `clear`：封装 `clear` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clear() {
        preferenceStore.remove(KEY_STANDARD_TAB_SESSION)
    }

    /**
     * 函数 `normalizeTabs`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabs 参数类型为 `List<BrowserTab>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeTabs(tabs: List<BrowserTab>): List<BrowserTab> {
        return tabs
            .mapNotNull { tab -> normalizeTab(tab) }
            .distinctBy { tab -> tab.id }
            .take(MAX_SESSION_TABS)
    }

    /**
     * 函数 `normalizeTab`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tab 参数类型为 `BrowserTab`，表示函数执行 `tab` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `normalizeRestorableWebUrl`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeRestorableWebUrl(url: String?): String? {
        return WebUrlNormalizer.normalizeHttpOrHttpsUrl(url)
    }

    /**
     * 函数 `renderSession`：封装 `render Session` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param activeTabId 参数类型为 `Long`，表示函数执行 `activeTabId` 相关逻辑时需要读取或处理的输入。
     * @param tabs 参数类型为 `List<BrowserTab>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun renderSession(activeTabId: Long, tabs: List<BrowserTab>): String {
        return buildString {
            append(activeTabId).append('\n')
            tabs.forEach { tab ->
                append(tab.id)
                    .append('\t')
                    .append(tab.createdAtMillis)
                    .append('\t')
                    .append(Utf8UrlCodec.encodeFormComponent(tab.url.orEmpty()))
                    .append('\t')
                    .append(Utf8UrlCodec.encodeFormComponent(tab.title))
                    .append('\n')
            }
        }
    }

    /**
     * 函数 `parseTabLine`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param line 参数类型为 `String`，表示函数执行 `line` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun parseTabLine(line: String): BrowserTab? {
        val parts = line.split('\t')
        if (parts.size != 4) {
            return null
        }
        val id = parts[0].toLongOrNull()?.takeIf { it > 0L } ?: return null
        val createdAtMillis = parts[1].toLongOrNull() ?: System.currentTimeMillis()
        val url = Utf8UrlCodec.decodeFormComponent(parts[2])?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val title = Utf8UrlCodec.decodeFormComponent(parts[3])?.trim().orEmpty()
        return BrowserTab(
            id = id,
            url = url,
            title = title.take(MAX_TITLE_LENGTH),
            createdAtMillis = createdAtMillis
        )
    }

    companion object {
        const val KEY_STANDARD_TAB_SESSION = "standard_tab_session"
        private const val MAX_SESSION_TABS = 50
        private const val MAX_TITLE_LENGTH = 200
    }
}
