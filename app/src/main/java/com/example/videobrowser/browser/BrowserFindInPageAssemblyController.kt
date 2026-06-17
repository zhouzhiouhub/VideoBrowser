package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“页内查找装配模块”。
 * 文件名 BrowserFindInPageAssemblyController 可以拆开理解为“Browser Find In Page Assembly Controller”，
 * 表示它只负责把当前 WebView 的查找能力接到 FindInPageController。
 * 阅读顺序：先看构造参数知道它如何取得当前 WebView 宿主，再看 create() 里的三个回调分别对应搜索、跳转匹配和清理匹配。
 */

/**
 * 页内查找控制器装配器。
 *
 * FindInPageController 本身只关心“查找全部、移动到下一处、清理匹配”这三个动作；
 * 本类把这些动作绑定到当前 BrowserManager，避免 MainActivity 直接写 WebView 查找细节。
 *
 * @param browserStandardWebViewHostController 参数类型为 `() -> BrowserStandardWebViewHostController`，
 * 表示延迟读取标准 WebView 宿主控制器的回调；延迟读取可以保留 MainActivity 原有的初始化顺序。
 */
class BrowserFindInPageAssemblyController(
    private val browserStandardWebViewHostController: () -> BrowserStandardWebViewHostController
) {
    /**
     * 创建页内查找控制器。
     *
     * @return 返回 `FindInPageController`，调用方把它交给查找弹窗执行搜索、下一处、上一处和清理动作。
     */
    fun create(): FindInPageController {
        return FindInPageController(
            findAll = { query ->
                browserStandardWebViewHostController().currentBrowserManager().findAllAsync(query)
            },
            findNext = { forward ->
                browserStandardWebViewHostController().currentBrowserManager().findNext(forward)
            },
            clearMatches = {
                browserStandardWebViewHostController().currentBrowserManager().clearFindMatches()
            }
        )
    }
}
