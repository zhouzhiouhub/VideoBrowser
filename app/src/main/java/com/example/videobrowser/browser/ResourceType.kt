package com.example.videobrowser.browser

/**
 * WebView 请求的轻量资源类型。UNKNOWN 表示当前信息不足，后续带类型限制的规则不应命中。
 */
enum class ResourceType {
    DOCUMENT,
    SCRIPT,
    IMAGE,
    STYLESHEET,
    MEDIA,
    FONT,
    XHR,
    FETCH,
    OTHER,
    UNKNOWN
}
