package com.example.videobrowser.functioncenter

data class BrowserCookieItem(
    val name: String
)

object BrowserCookieParser {
    /**
     * 函数 `parse`：把 Cookie header 转换成只包含名称的展示模型，避免 UI 暴露 cookie 值。
     */
    fun parse(rawCookieHeader: String?): List<BrowserCookieItem> {
        return rawCookieHeader
            ?.split(';')
            ?.mapNotNull(::parseCookiePart)
            ?.distinctBy { cookie -> cookie.name }
            ?: emptyList()
    }

    private fun parseCookiePart(part: String): BrowserCookieItem? {
        val trimmed = part.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val separatorIndex = trimmed.indexOf('=')
        if (separatorIndex <= 0) {
            return null
        }
        val name = trimmed.substring(0, separatorIndex).trim()
        if (name.isEmpty()) {
            return null
        }
        return BrowserCookieItem(name = name)
    }
}
