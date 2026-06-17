package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“标准标签页会话模块”。
 * BrowserTabStore 只保存内存中的标签页列表，BrowserTabSessionRepository 负责读写持久化文本；
 * 本类把“从仓库恢复到内存”和“从内存保存到仓库”这两段接线逻辑从 MainActivity 中拆出来。
 * 阅读顺序：先看构造参数，再看 restoreStandardTabSession() 和 saveStandardTabSession() 的数据流。
 */

/**
 * 标准标签页会话控制器。
 *
 * 这个类只处理普通浏览模式的标签页恢复和保存，不处理无痕标签页，也不直接操作 WebView。
 *
 * @param browserTabSessionRepository 参数类型为 `() -> BrowserTabSessionRepository?`，表示返回标签页会话仓库的函数；仓库尚未初始化时返回 null。
 * @param standardTabStore 参数类型为 `BrowserTabStore`，表示标准浏览模式的内存标签页列表，用于恢复标签页和读取当前待保存状态。
 */
class BrowserStandardTabSessionController(
    private val browserTabSessionRepository: () -> BrowserTabSessionRepository?,
    private val standardTabStore: BrowserTabStore
) {
    /**
     * 从持久化仓库恢复标准标签页会话。
     *
     * @return 无返回值；没有可恢复会话或仓库尚未初始化时保持当前默认标签页。
     */
    fun restoreStandardTabSession() {
        browserTabSessionRepository()?.restore()?.let { session ->
            standardTabStore.restore(session.tabs, session.activeTabId)
        }
    }

    /**
     * 把当前标准标签页会话保存到持久化仓库。
     *
     * @return 无返回值；仓库尚未初始化时直接跳过。
     */
    fun saveStandardTabSession() {
        val repository = browserTabSessionRepository() ?: return
        repository.save(
            tabs = standardTabStore.tabs(),
            activeTabId = standardTabStore.activeTabId
        )
    }
}
