package com.example.videobrowser.adblock

/**
 * 初学者阅读提示：
 * 这个文件属于“广告请求拦截模块”。
 * 文件名 EmptyResponseFactory 可以拆开理解为“Empty Response Factory”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：把 WebView 的网络请求交给规则系统判断，并在需要拦截时返回安全的本地响应。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

/**
 * 统一创建广告拦截命中后的空响应，避免 UI 层关心响应状态码和空 body 细节。
 */
object EmptyResponseFactory {
    private val EMPTY_BODY = ByteArray(0)

    /**
     * 函数 `noContent`：封装 `no Content` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun noContent(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            ByteArrayInputStream(EMPTY_BODY)
        ).apply {
            setStatusCodeAndReasonPhrase(204, "No Content")
        }
    }
}
