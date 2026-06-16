package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserTabStore 可以拆开理解为“Browser Tab Store”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
/**
 * 纯内存的标签页列表。
 *
 * 这里不操作 WebView，只维护 BrowserTab 数据：当前标签、关闭标签、恢复最近关闭标签。
 * 这样标签页行为可以用普通单元测试验证，不需要启动 Android 设备。
 */
class BrowserTabStore(
    private val idGenerator: () -> Long = IdSequence()
) {
    private val tabs = mutableListOf(BrowserTab(id = idGenerator()))
    private val recentlyClosedTabs = mutableListOf<BrowserTab>()

    var activeTabId: Long = tabs.first().id
        private set

    /**
     * 函数 `tabs`：封装 `tabs` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun tabs(): List<BrowserTab> {
        return tabs.toList()
    }

    /**
     * 函数 `activeTab`：封装 `active Tab` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun activeTab(): BrowserTab {
        return tabs.first { tab -> tab.id == activeTabId }
    }

    /**
     * 函数 `openTab`：启动或加载 `open Tab` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun openTab(url: String? = null, title: String = ""): BrowserTab {
        val tab = BrowserTab(
            id = nextUniqueId(),
            url = url,
            title = title
        )
        tabs += tab
        activeTabId = tab.id
        return tab
    }

    /**
     * 函数 `switchTo`：封装 `switch To` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabId 参数类型为 `Long`，表示函数执行 `tabId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun switchTo(tabId: Long): Boolean {
        if (tabs.none { tab -> tab.id == tabId }) {
            return false
        }
        activeTabId = tabId
        return true
    }

    /**
     * 函数 `closeTab`：控制 `close Tab` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabId 参数类型为 `Long`，表示函数执行 `tabId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun closeTab(tabId: Long): Boolean {
        // 浏览器至少保留一个标签页，避免 UI 没有 activeTab 可以显示。
        if (tabs.size == 1) {
            return false
        }
        val index = tabs.indexOfFirst { tab -> tab.id == tabId }
        if (index < 0) {
            return false
        }
        val closedTab = tabs.removeAt(index)
        rememberClosedTabs(listOf(closedTab))
        if (activeTabId == tabId) {
            activeTabId = tabs.getOrNull(index)?.id ?: tabs.last().id
        }
        return true
    }

    /**
     * 函数 `closeOtherTabs`：控制 `close Other Tabs` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabId 参数类型为 `Long`，表示函数执行 `tabId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun closeOtherTabs(tabId: Long): List<BrowserTab> {
        val targetTab = tabs.firstOrNull { tab -> tab.id == tabId } ?: return emptyList()
        val closedTabs = tabs.filter { tab -> tab.id != tabId }
        if (closedTabs.isEmpty()) {
            return emptyList()
        }

        rememberClosedTabs(closedTabs)
        tabs.clear()
        tabs += targetTab
        activeTabId = tabId
        return closedTabs
    }

    /**
     * 函数 `closeAllTabs`：控制 `close All Tabs` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun closeAllTabs(): List<BrowserTab> {
        val closedTabs = tabs.toList()
        rememberClosedTabs(closedTabs)
        val blankTab = BrowserTab(id = nextUniqueId())
        tabs.clear()
        tabs += blankTab
        activeTabId = blankTab.id
        return closedTabs
    }

    /**
     * 函数 `canReopenClosedTab`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun canReopenClosedTab(): Boolean {
        return recentlyClosedTabs.isNotEmpty()
    }

    /**
     * 函数 `reopenClosedTab`：封装 `reopen Closed Tab` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun reopenClosedTab(): BrowserTab? {
        if (recentlyClosedTabs.isEmpty()) {
            return null
        }
        val closedTab = recentlyClosedTabs.removeAt(recentlyClosedTabs.lastIndex)
        return openTab(url = closedTab.url, title = closedTab.title)
    }

    /**
     * 函数 `updateActiveTab`：根据最新状态刷新 `update Active Tab` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    fun updateActiveTab(url: String? = activeTab().url, title: String = activeTab().title) {
        updateTab(activeTabId, url, title)
    }

    /**
     * 函数 `updateTab`：根据最新状态刷新 `update Tab` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param tabId 参数类型为 `Long`，表示函数执行 `tabId` 相关逻辑时需要读取或处理的输入。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun updateTab(tabId: Long, url: String?, title: String): Boolean {
        val index = tabs.indexOfFirst { tab -> tab.id == tabId }
        if (index < 0) {
            return false
        }
        tabs[index] = tabs[index].copy(url = url, title = title)
        return true
    }

    /**
     * 函数 `restore`：封装 `restore` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param restoredTabs 参数类型为 `List<BrowserTab>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @param restoredActiveTabId 参数类型为 `Long?`，表示函数执行 `restoredActiveTabId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun restore(restoredTabs: List<BrowserTab>, restoredActiveTabId: Long?): Boolean {
        val normalizedTabs = restoredTabs
            .filter { tab -> tab.id > 0L }
            .distinctBy { tab -> tab.id }
            .takeIf { it.isNotEmpty() }
            ?: return false

        tabs.clear()
        tabs.addAll(normalizedTabs)
        recentlyClosedTabs.clear()
        activeTabId = restoredActiveTabId
            ?.takeIf { tabId -> tabs.any { tab -> tab.id == tabId } }
            ?: tabs.first().id
        return true
    }

    /**
     * 函数 `rememberClosedTabs`：封装 `remember Closed Tabs` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param closedTabs 参数类型为 `List<BrowserTab>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     */
    private fun rememberClosedTabs(closedTabs: List<BrowserTab>) {
        // 最近关闭列表只保留有限数量，防止用户长时间使用后内存无限增长。
        recentlyClosedTabs.addAll(closedTabs)
        if (recentlyClosedTabs.size > MAX_RECENTLY_CLOSED_TABS) {
            recentlyClosedTabs.subList(0, recentlyClosedTabs.size - MAX_RECENTLY_CLOSED_TABS).clear()
        }
    }

    /**
     * 函数 `nextUniqueId`：封装 `next Unique Id` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun nextUniqueId(): Long {
        while (true) {
            val candidate = idGenerator()
            if (tabs.none { tab -> tab.id == candidate }) {
                return candidate
            }
        }
    }

    private class IdSequence : () -> Long {
        private var nextId = 1L

        /**
         * 函数 `invoke`：封装 `invoke` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun invoke(): Long {
            return nextId++
        }
    }

    private companion object {
        private const val MAX_RECENTLY_CLOSED_TABS = 20
    }
}
