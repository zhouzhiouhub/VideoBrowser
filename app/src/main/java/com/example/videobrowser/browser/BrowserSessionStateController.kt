package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器会话状态模块”。
 * 文件名 BrowserSessionStateController 可以拆开理解为“Browser Session State Controller”，
 * 表示它只负责在普通/无痕两套会话控制器之间选择当前会话，并判断会话是否已经准备好。
 * 阅读顺序：先看构造参数了解它如何延迟获取会话，再看 currentSessionController() 的选择规则。
 */

/**
 * 浏览器会话状态控制器。
 *
 * MainActivity 会在较早阶段创建很多回调，但标准/无痕会话控制器稍后才初始化。
 * 因此本类接收可空 provider，在真正需要当前会话时再读取对应控制器。
 *
 * @param isPrivateBrowsingActive 返回当前是否处于无痕浏览模式的函数，true 表示选择无痕会话。
 * @param standardSessionController 返回普通浏览会话控制器的函数，尚未初始化时返回 null。
 * @param privateSessionController 返回无痕浏览会话控制器的函数，尚未初始化时返回 null。
 */
class BrowserSessionStateController(
    private val isPrivateBrowsingActive: () -> Boolean,
    private val standardSessionController: () -> BrowserSessionController?,
    private val privateSessionController: () -> BrowserSessionController?
) {
    /**
     * 返回当前浏览模式对应的会话控制器。
     *
     * @return 当前模式对应的 BrowserSessionController；普通模式返回标准会话，无痕模式返回无痕会话。
     */
    fun currentSessionController(): BrowserSessionController {
        val selectedController = if (isPrivateBrowsingActive()) {
            privateSessionController()
        } else {
            standardSessionController()
        }
        return checkNotNull(selectedController) {
            "Browser session controller has not been initialized for the current browsing mode."
        }
    }

    /**
     * 判断标准和无痕两套会话控制器是否都已初始化。
     *
     * @return true 表示 currentSessionController 可以根据当前模式安全返回会话控制器。
     */
    fun areBrowserSessionsInitialized(): Boolean {
        return standardSessionController() != null && privateSessionController() != null
    }
}
