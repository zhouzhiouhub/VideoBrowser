package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“ChromeClient 状态装配模块”。
 * 文件名 BrowserChromeClientStateAssemblyController 可以拆开理解为
 * “Browser ChromeClient State Assembly Controller”，表示它只负责创建保存当前 ChromeClient 状态读取方式的控制器。
 * 阅读顺序：先看构造参数里的 provider，再看 create() 如何把 provider 交给 BrowserChromeClientStateController。
 */

/**
 * ChromeClient 状态控制器装配器。
 *
 * BrowserChromeClientStateController 需要在多个模块之间共享当前 ChromeClient；
 * 本类封装 MainActivity 里的 lateinit 安全读取逻辑，让 Activity 不再展开这一段细节。
 *
 * @param browserChromeClientController 参数类型为 `() -> BrowserChromeClientController?`，
 * 表示安全读取 ChromeClient 装配控制器的回调；尚未初始化时返回 null。
 */
class BrowserChromeClientStateAssemblyController(
    private val browserChromeClientController: () -> BrowserChromeClientController?
) {
    /**
     * 创建 ChromeClient 状态控制器。
     *
     * @return 返回 `BrowserChromeClientStateController`，其它浏览器模块通过它读取或同步当前 ChromeClient。
     */
    fun create(): BrowserChromeClientStateController {
        return BrowserChromeClientStateController(
            browserChromeClientController = browserChromeClientController
        )
    }
}
