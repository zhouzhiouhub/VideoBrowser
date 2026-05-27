package com.example.videobrowser.adblock

import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

/**
 * 统一创建广告拦截命中后的空响应，避免 UI 层关心响应状态码和空 body 细节。
 */
object EmptyResponseFactory {
    private val EMPTY_BODY = ByteArray(0)

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
