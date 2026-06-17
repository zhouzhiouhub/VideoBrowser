package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器标签页状态装配模块”。
 * 文件名 BrowserTabStateAssemblyController 可以拆开理解为“Browser Tab State Assembly Controller”，
 * 表示它只负责创建普通/无痕两套标签页数据和对应的会话绑定对象。
 * 阅读顺序：先看 BrowserTabStateComponents 了解会返回哪些对象，再看 create() 如何保证 binding 使用对应的 tab store。
 */

/**
 * 浏览器标签页状态组件集合。
 *
 * @param standardTabStore 参数类型为 `BrowserTabStore`，表示标准浏览模式的标签页数据容器。
 * @param privateTabStore 参数类型为 `BrowserTabStore`，表示无痕浏览模式的标签页数据容器。
 * @param standardTabSessionBinding 参数类型为 `BrowserTabSessionBinding`，表示标准标签页元数据同步绑定。
 * @param privateTabSessionBinding 参数类型为 `BrowserTabSessionBinding`，表示无痕标签页元数据同步绑定。
 */
data class BrowserTabStateComponents(
    val standardTabStore: BrowserTabStore,
    val privateTabStore: BrowserTabStore,
    val standardTabSessionBinding: BrowserTabSessionBinding,
    val privateTabSessionBinding: BrowserTabSessionBinding
)

/**
 * 浏览器标签页状态装配器。
 *
 * MainActivity 不需要知道标准/无痕 store 与 binding 的创建细节；
 * 本类集中创建并保证每个 binding 绑定到对应浏览模式的 BrowserTabStore。
 */
class BrowserTabStateAssemblyController {
    /**
     * 创建标签页状态组件。
     *
     * @return 返回 `BrowserTabStateComponents`，调用方把其中的 store 和 binding 分发给会话、导航和持久化模块。
     */
    fun create(): BrowserTabStateComponents {
        val standardTabStore = BrowserTabStore()
        val privateTabStore = BrowserTabStore()
        return BrowserTabStateComponents(
            standardTabStore = standardTabStore,
            privateTabStore = privateTabStore,
            standardTabSessionBinding = BrowserTabSessionBinding(standardTabStore),
            privateTabSessionBinding = BrowserTabSessionBinding(privateTabStore)
        )
    }
}
