package com.example.videobrowser.site

/**
 * 初学者阅读提示：
 * 这个文件属于“站点适配模块”。
 * 文件名 SiteAdapter 可以拆开理解为“Site Adapter”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：识别不同视频网站或网页宿主，并把站点专属能力交给通用浏览器流程使用。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
interface SiteAdapter {
    val profile: SiteProfile

    fun matches(url: String): Boolean

    fun scriptFiles(): List<String> {
        return profile.scriptAssetPaths
    }

    fun cssSelectors(): List<String> {
        return profile.cssSelectors
    }

    fun domSelectors(): List<String> {
        return profile.domSelectors
    }
}
