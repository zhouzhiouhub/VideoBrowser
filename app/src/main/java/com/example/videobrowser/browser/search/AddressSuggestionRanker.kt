package com.example.videobrowser.browser.search

/**
 * 初学者阅读提示：
 * 这个文件属于“搜索入口与地址建议模块”。
 * 文件名 AddressSuggestionRanker 可以拆开理解为“Address Suggestion Ranker”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：把地址栏输入、默认搜索引擎、远程搜索建议、收藏和历史候选项整理成用户可以点击的建议列表。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.utils.UrlUtils
import java.util.Locale

/**
 * 地址建议排序器。
 *
 * 它是纯函数对象：输入关键词、历史、收藏、远程关键词，输出最多 DEFAULT_LIMIT 条建议。
 * 因为不依赖 Android UI，所以适合用单元测试覆盖排序规则。
 */
object AddressSuggestionRanker {
    /**
     * 函数 `build`：创建 `build` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param input 参数类型为 `String`，表示函数执行 `input` 相关逻辑时需要读取或处理的输入。
     * @param history 参数类型为 `List<SavedPage>`，表示函数执行 `history` 相关逻辑时需要读取或处理的输入。
     * @param bookmarks 参数类型为 `List<SavedPage>`，表示函数执行 `bookmarks` 相关逻辑时需要读取或处理的输入。
     * @param remoteKeywords 参数类型为 `List<String>`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param includePrivateSources 参数类型为 `Boolean`，表示函数执行 `includePrivateSources` 相关逻辑时需要读取或处理的输入。
     * @param limit 参数类型为 `Int`，表示函数执行 `limit` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun build(
        input: String,
        history: List<SavedPage>,
        bookmarks: List<SavedPage> = emptyList(),
        remoteKeywords: List<String>,
        includePrivateSources: Boolean,
        limit: Int = DEFAULT_LIMIT
    ): List<AddressSuggestion> {
        val keyword = input.trim()
        if (keyword.isEmpty() || limit <= 0) {
            return emptyList()
        }

        val fallback = AddressSuggestion.Fallback(keyword)
        if (!includePrivateSources || limit == 1) {
            // 无痕或只允许一条结果时，只返回“搜索当前输入”的兜底项。
            return listOf(fallback)
        }

        val normalizedInput = normalize(keyword)
        val seenUrls = linkedSetOf<String>()
        val bookmarkSuggestions = savedPageSuggestions(
            pages = bookmarks,
            normalizedInput = normalizedInput,
            seenUrls = seenUrls
        ) { page, displayUrl ->
            AddressSuggestion.Bookmark(
                title = page.title,
                url = page.url,
                displayUrl = displayUrl
            )
        }
        val historySuggestions = savedPageSuggestions(
            pages = history,
            normalizedInput = normalizedInput,
            seenUrls = seenUrls
        ) { page, displayUrl ->
            AddressSuggestion.History(
                title = page.title,
                url = page.url,
                displayUrl = displayUrl
            )
        }
        val remoteSuggestions = remoteSuggestions(remoteKeywords, normalizedInput)
        // 兜底搜索项永远放最后，让用户随时能按原始输入发起搜索。
        return (bookmarkSuggestions + historySuggestions + remoteSuggestions)
            .take(limit - 1) + fallback
    }

    /**
     * 函数 `savedPageSuggestions`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pages 参数类型为 `List<SavedPage>`，表示函数执行 `pages` 相关逻辑时需要读取或处理的输入。
     * @param normalizedInput 参数类型为 `String`，表示函数执行 `normalizedInput` 相关逻辑时需要读取或处理的输入。
     * @param seenUrls 参数类型为 `MutableSet<String>`，表示函数执行 `seenUrls` 相关逻辑时需要读取或处理的输入。
     * @param createSuggestion 参数类型为 `(SavedPage, String) -> T`，表示函数执行 `createSuggestion` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun <T : AddressSuggestion> savedPageSuggestions(
        pages: List<SavedPage>,
        normalizedInput: String,
        seenUrls: MutableSet<String>,
        createSuggestion: (SavedPage, String) -> T
    ): List<T> {
        return pages.mapNotNull { page ->
            val displayUrl = UrlUtils.displayUrl(page.url)
            val matches = normalize(page.title).contains(normalizedInput) ||
                normalize(displayUrl).contains(normalizedInput) ||
                normalize(page.folder).contains(normalizedInput)
            val normalizedUrl = page.url.lowercase(Locale.ROOT)
            if (!matches || !seenUrls.add(normalizedUrl)) {
                null
            } else {
                createSuggestion(page, displayUrl)
            }
        }
    }

    /**
     * 函数 `remoteSuggestions`：封装 `remote Suggestions` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param remoteKeywords 参数类型为 `List<String>`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param normalizedInput 参数类型为 `String`，表示函数执行 `normalizedInput` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun remoteSuggestions(
        remoteKeywords: List<String>,
        normalizedInput: String
    ): List<AddressSuggestion.Remote> {
        val seenKeywords = linkedSetOf<String>()
        return remoteKeywords.mapNotNull { rawKeyword ->
            val keyword = rawKeyword.trim()
            val normalizedKeyword = normalize(keyword)
            if (
                keyword.isEmpty() ||
                normalizedKeyword == normalizedInput ||
                !seenKeywords.add(normalizedKeyword)
            ) {
                null
            } else {
                AddressSuggestion.Remote(keyword)
            }
        }
    }

    /**
     * 函数 `normalize`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalize(value: String): String {
        return value.trim().lowercase(Locale.ROOT)
    }

    private const val DEFAULT_LIMIT = 6
}
