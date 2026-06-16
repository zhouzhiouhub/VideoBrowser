package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 SmartNoImageRequestInterceptor 可以拆开理解为“Smart No Image Request Interceptor”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.webkit.WebResourceResponse
import com.example.videobrowser.adblock.EmptyResponseFactory

class SmartNoImageRequestInterceptor(
    private val isEnabled: () -> Boolean,
    private val isDisabledForCurrentSite: () -> Boolean,
    private val currentPageUrl: () -> String?
) {
    /**
     * 函数 `intercept`：封装 `intercept` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param request 参数类型为 `BrowserRequest`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun intercept(request: BrowserRequest): WebResourceResponse? {
        val context = RequestContext.from(
            request = request,
            pageUrl = request.pageUrl ?: currentPageUrl()
        )
        if (!SmartNoImageRequestPolicy.shouldBlock(
                enabled = isEnabled(),
                siteSmartNoImageDisabled = isDisabledForCurrentSite(),
                context = context
            )
        ) {
            return null
        }
        return EmptyResponseFactory.noContent()
    }
}
