package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 WebViewClient 装配模块”。
 * BrowserClient 是 WebViewClient 的应用适配层；本控制器负责创建 BrowserClient，
 * 并把页面状态、错误页、请求拦截、证书、HTTP 认证和渲染进程恢复接到各自模块。
 * 阅读顺序：先看 setupBrowserClient，再看 showBrowserErrorPage，最后看各个私有回调函数。
 */
import android.net.Uri
import android.webkit.WebView
import com.example.videobrowser.adblock.AdBlockRequestInterceptor

/**
 * Browser WebViewClient 控制器。
 *
 * MainActivity 只传入必须留在 Activity 层的策略函数；具体 BrowserClient 回调装配和清理逻辑由本类维护。
 *
 * @param browserManager 返回当前 BrowserManager 的函数，用来给 active WebView 设置 BrowserClient 和加载错误页。
 * @param sessionController 返回当前 BrowserSessionController 的函数，用来同步页面开始、完成和失败状态。
 * @param resetBackExitConfirmation 重置“再次返回退出”确认状态的函数，页面开始加载时调用。
 * @param renderProcessRecoveryController WebView 渲染进程退出恢复控制器。
 * @param clientCertificateController 客户端证书选择控制器，负责系统证书选择器和待处理请求清理。
 * @param httpAuthController HTTP Basic Auth 控制器，负责认证弹窗和待处理 handler 清理。
 * @param adBlockRequestInterceptor 广告拦截请求处理器，优先决定是否拦截资源请求。
 * @param smartNoImageRequestInterceptor 智能无图请求处理器，在广告拦截未命中时尝试拦截图片请求。
 * @param shouldBlockUrl URL 加载决策函数，处理外部协议、媒体路由和导航安全确认。
 */
class BrowserWebClientController(
    private val browserManager: () -> BrowserManager,
    private val sessionController: () -> BrowserSessionController,
    private val resetBackExitConfirmation: () -> Unit,
    private val renderProcessRecoveryController: RenderProcessRecoveryController,
    private val clientCertificateController: ClientCertificateController,
    private val httpAuthController: HttpAuthController,
    private val adBlockRequestInterceptor: AdBlockRequestInterceptor,
    private val smartNoImageRequestInterceptor: SmartNoImageRequestInterceptor,
    private val shouldBlockUrl: (WebView?, Uri, Boolean) -> Boolean
) {
    /**
     * 创建 BrowserClient，并把它设置到当前 active WebView。
     */
    fun setupBrowserClient() {
        browserManager().setBrowserClient(
            BrowserClient(
                pageStarted = { url ->
                    resetBackExitConfirmation()
                    sessionController().handlePageStarted(url)
                },
                pageFinished = { url -> sessionController().handlePageFinished(url) },
                pageLoadFailed = ::showBrowserErrorPage,
                requestIntercepted = ::interceptBrowserRequest,
                urlLoadingRequested = shouldBlockUrl,
                clientCertRequested = { _, request -> clientCertificateController.handleRequest(request) },
                renderProcessGone = renderProcessRecoveryController::handleRenderProcessGone,
                httpAuthRequested = { _, handler, host, realm ->
                    httpAuthController.handleRequest(handler, host, realm)
                }
            )
        )
    }

    /**
     * 显示浏览器错误页，并同步当前会话的失败 URL。
     *
     * @param error 浏览器加载失败、证书错误、安全浏览拦截或渲染进程退出的错误信息。
     */
    fun showBrowserErrorPage(error: BrowserPageError) {
        sessionController().handlePageFailed(error.url)
        browserManager().loadErrorPage(error)
    }

    /**
     * 取消正在等待用户输入的 HTTP Basic Auth 请求。
     */
    fun cancelPendingHttpAuthRequest() {
        httpAuthController.cancelPending()
    }

    /**
     * 取消正在等待系统证书选择器或证书读取结果的客户端证书请求。
     */
    fun cancelPendingClientCertRequest() {
        clientCertificateController.cancelPending()
    }

    /**
     * 依次执行广告拦截和智能无图拦截。
     *
     * @param request WebView 发出的资源请求。
     * @return 返回非空值表示该请求已被本地响应拦截；返回 null 表示继续交给 WebView 加载。
     */
    private fun interceptBrowserRequest(request: BrowserRequest) =
        adBlockRequestInterceptor.intercept(request) ?: smartNoImageRequestInterceptor.intercept(request)
}
