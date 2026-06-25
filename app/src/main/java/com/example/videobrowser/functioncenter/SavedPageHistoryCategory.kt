package com.example.videobrowser.functioncenter

import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.utils.MediaUrlUtils

/**
 * 浏览历史页顶部类型标签的筛选规则。
 */
internal enum class SavedPageHistoryCategory(val labelRes: Int) {
    SEARCH(R.string.history_category_search),
    URL(R.string.history_category_url),
    TOOL(R.string.history_category_tools),
    VIDEO(R.string.history_category_video);

    fun filter(pages: List<SavedPage>): List<SavedPage> {
        return when (this) {
            SEARCH -> pages
            URL -> pages.filter(::isUrlOnlyPage)
            TOOL -> emptyList()
            VIDEO -> pages.filter(::isVideoPage)
        }
    }

    private fun isUrlOnlyPage(page: SavedPage): Boolean {
        val title = page.title.trim()
        return title.isEmpty() || title.equals(page.url, ignoreCase = true)
    }

    private fun isVideoPage(page: SavedPage): Boolean {
        if (MediaUrlUtils.isPlayableMediaUri(page.url)) {
            return true
        }
        val text = "${page.title}\n${page.url}".lowercase()
        return videoKeywords.any { keyword -> text.contains(keyword) }
    }

    private companion object {
        private val videoKeywords = listOf(
            "video",
            "bilibili",
            "youtube",
            "youku",
            "iqiyi",
            "mgtv",
            "v.qq.com",
            "\u89c6\u9891"
        )
    }
}
