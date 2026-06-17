package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器会话状态装配模块”。
 * 文件名 BrowserSessionStateAssemblyController 可以拆开理解为“Browser Session State Assembly Controller”，
 * 表示它只负责创建能在普通/无痕会话之间做选择的状态控制器。
 * 阅读顺序：先看构造参数理解普通会话、无痕会话和当前模式如何被延迟读取，再看 create() 的连接方式。
 */

/**
 * 浏览器会话状态控制器装配器。
 *
 * 标准会话和无痕会话在 Activity 初始化后段才创建；但很多早期组件已经需要持有“当前会话”读取入口。
 * 本类保留可空 provider，把直接构造 BrowserSessionStateController 的细节从 MainActivity 中移出。
 *
 * @param isPrivateBrowsingActive 参数类型为 `() -> Boolean`，表示读取当前是否处于无痕浏览模式的回调。
 * @param standardSessionController 参数类型为 `() -> BrowserSessionController?`，表示安全读取标准浏览会话控制器的回调。
 * @param privateSessionController 参数类型为 `() -> BrowserSessionController?`，表示安全读取无痕浏览会话控制器的回调。
 */
class BrowserSessionStateAssemblyController(
    private val isPrivateBrowsingActive: () -> Boolean,
    private val standardSessionController: () -> BrowserSessionController?,
    private val privateSessionController: () -> BrowserSessionController?
) {
    /**
     * 创建浏览器会话状态控制器。
     *
     * @return 返回 `BrowserSessionStateController`，调用方通过它读取当前模式对应的会话控制器。
     */
    fun create(): BrowserSessionStateController {
        return BrowserSessionStateController(
            isPrivateBrowsingActive = isPrivateBrowsingActive,
            standardSessionController = standardSessionController,
            privateSessionController = privateSessionController
        )
    }
}
