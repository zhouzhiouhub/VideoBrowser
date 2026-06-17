package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 ChromeClient 状态模块”。
 * 文件名 BrowserChromeClientStateController 可以拆开理解为“Browser ChromeClient State Controller”，
 * 表示它只负责安全读取当前 ChromeClient，并把初始化状态判断从 MainActivity 中移走。
 * 阅读顺序：先看 currentChromeClientOrNull() 了解安全读取方式，再看 syncCurrentChromeClientIfReady()。
 */

/**
 * Browser ChromeClient 状态控制器。
 *
 * BrowserChromeClientController 会在 WebView、权限和窗口控制器都准备好之后才创建。
 * 本类通过可空 provider 延迟读取它，让较早创建的回调可以安全持有本类方法引用。
 *
 * @param browserChromeClientController 返回 BrowserChromeClientController 的函数，尚未初始化时返回 null。
 */
class BrowserChromeClientStateController(
    private val browserChromeClientController: () -> BrowserChromeClientController?
) {
    /**
     * 返回当前浏览模式对应的 ChromeClient。
     *
     * @return 当前 ChromeClient；调用前必须保证 areChromeClientsInitialized() 为 true。
     */
    fun currentChromeClient(): ChromeClient {
        return requireNotNull(browserChromeClientController()) {
            "BrowserChromeClientController has not been initialized."
        }.currentChromeClient()
    }

    /**
     * 安全返回当前 ChromeClient。
     *
     * @return 如果 BrowserChromeClientController 和两套 ChromeClient 都已初始化则返回当前 ChromeClient，否则返回 null。
     */
    fun currentChromeClientOrNull(): ChromeClient? {
        val controller = browserChromeClientController() ?: return null
        if (!controller.areChromeClientsInitialized()) {
            return null
        }
        return controller.currentChromeClient()
    }

    /**
     * 判断标准和无痕两套 ChromeClient 是否都已创建。
     *
     * @return true 表示 currentChromeClient() 可以安全返回当前模式的 ChromeClient。
     */
    fun areChromeClientsInitialized(): Boolean {
        return browserChromeClientController()?.areChromeClientsInitialized() == true
    }

    /**
     * 在可用时把当前模式 ChromeClient 重新绑定到 active WebView。
     *
     * @return 无返回值；BrowserChromeClientController 尚未初始化时不做任何操作。
     */
    fun syncCurrentChromeClientIfReady() {
        browserChromeClientController()?.syncCurrentChromeClient()
    }
}
