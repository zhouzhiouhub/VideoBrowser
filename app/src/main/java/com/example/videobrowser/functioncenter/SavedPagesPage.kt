package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 SavedPagesPage 可以拆开理解为“Saved Pages Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.storage.SavedPageSearch
import com.example.videobrowser.utils.ShortDateTimeFormatter
import com.example.videobrowser.utils.UrlUtils

class SavedPagesPage(
    private val host: FunctionCenterPageHost,
    private val savedPageRepository: SavedPageRepository,
    private val openUrlInNewTab: (String) -> Unit,
    private val loadUrl: (String) -> Unit,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity
    private val dialogController = SavedPagesDialogController(
        activity = activity,
        savedPageRepository = savedPageRepository,
        linkActions = SavedPageLinkActions(activity),
        openUrlInNewTab = openUrlInNewTab,
        loadUrl = loadUrl,
        showSavedPagesPage = ::show
    )

    /**
     * 函数 `show`：控制 `show` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param collection 参数类型为 `SavedPageCollection`，表示函数执行 `collection` 相关逻辑时需要读取或处理的输入。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param emptyMessage 参数类型为 `String`，表示函数执行 `emptyMessage` 相关逻辑时需要读取或处理的输入。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     * @param query 参数类型为 `String?`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     */
    fun show(
        collection: SavedPageCollection,
        title: String,
        emptyMessage: String,
        replaceCurrent: Boolean = false,
        query: String? = null
    ) {
        val allPages = savedPageRepository.pages(collection)
        val pages = SavedPageSearch.filter(allPages, query)

        host.showPage(
            title = title,
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (allPages.isEmpty()) {
                host.addEmptyState(content, emptyMessage)
                return@showPage
            }

            host.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_search_saved_pages),
                    summary = currentSearchSummary(query)
                ) {
                    dialogController.showSearchDialog(collection, title, emptyMessage, query)
                }
                if (!query.isNullOrBlank()) {
                    host.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_clear_search),
                        summary = query
                    ) {
                        show(
                            collection = collection,
                            title = title,
                            emptyMessage = emptyMessage,
                            replaceCurrent = true
                        )
                    }
                }
                host.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_saved_pages_summary)
                ) {
                    dialogController.showClearSavedPagesDialog(collection)
                }
            }

            host.addFunctionSection(
                parent = content,
                title = activity.getString(R.string.function_center_section_records)
            ) { section ->
                if (pages.isEmpty()) {
                    host.addEmptyState(section, activity.getString(R.string.dialog_saved_pages_search_empty))
                    return@addFunctionSection
                }
                if (collection == SavedPageCollection.BOOKMARKS) {
                    addBookmarkGroups(section, pages, title, emptyMessage)
                } else {
                    addSavedPageRows(section, collection, pages, title, emptyMessage)
                }
            }
        }
    }

    /**
     * 函数 `addBookmarkGroups`：封装 `add Bookmark Groups` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param section 参数类型为 `android.widget.LinearLayout`，表示函数执行 `section` 相关逻辑时需要读取或处理的输入。
     * @param pages 参数类型为 `List<SavedPage>`，表示函数执行 `pages` 相关逻辑时需要读取或处理的输入。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param emptyMessage 参数类型为 `String`，表示函数执行 `emptyMessage` 相关逻辑时需要读取或处理的输入。
     */
    private fun addBookmarkGroups(
        section: android.widget.LinearLayout,
        pages: List<SavedPage>,
        title: String,
        emptyMessage: String
    ) {
        bookmarkGroups(pages).forEachIndexed { index, group ->
            if (index > 0) {
                host.addDivider(section)
            }
            host.addInfoRow(
                parent = section,
                title = group.title,
                summary = activity.getString(R.string.bookmark_folder_count, group.pages.size)
            )
            addSavedPageRows(section, SavedPageCollection.BOOKMARKS, group.pages, title, emptyMessage)
        }
    }

    /**
     * 函数 `addSavedPageRows`：封装 `add Saved Page Rows` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param section 参数类型为 `android.widget.LinearLayout`，表示函数执行 `section` 相关逻辑时需要读取或处理的输入。
     * @param collection 参数类型为 `SavedPageCollection`，表示函数执行 `collection` 相关逻辑时需要读取或处理的输入。
     * @param pages 参数类型为 `List<SavedPage>`，表示函数执行 `pages` 相关逻辑时需要读取或处理的输入。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param emptyMessage 参数类型为 `String`，表示函数执行 `emptyMessage` 相关逻辑时需要读取或处理的输入。
     */
    private fun addSavedPageRows(
        section: android.widget.LinearLayout,
        collection: SavedPageCollection,
        pages: List<SavedPage>,
        title: String,
        emptyMessage: String
    ) {
        pages.forEach { page ->
            host.addActionRow(
                parent = section,
                title = page.title.ifBlank { page.url },
                summary = pageSummary(page)
            ) {
                dialogController.showSavedPageActionsDialog(collection, page, title, emptyMessage)
            }
        }
    }

    /**
     * 函数 `pageSummary`：封装 `page Summary` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param page 参数类型为 `SavedPage`，表示函数执行 `page` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun pageSummary(page: SavedPage): String {
        val timestamp = page.updatedAtMillis.takeIf { it > 0L }
        return listOfNotNull(
            page.folder.takeIf { it.isNotBlank() }?.let { folder ->
                activity.getString(R.string.bookmark_folder_summary, folder)
            },
            UrlUtils.displayUrl(page.url),
            timestamp?.let(ShortDateTimeFormatter::format)
        ).joinToString(" | ")
    }

    /**
     * 函数 `currentSearchSummary`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param query 参数类型为 `String?`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun currentSearchSummary(query: String?): String {
        return query
            ?.takeIf { it.isNotBlank() }
            ?: activity.getString(R.string.action_search_saved_pages_summary)
    }

    /**
     * 函数 `bookmarkGroups`：封装 `bookmark Groups` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pages 参数类型为 `List<SavedPage>`，表示函数执行 `pages` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun bookmarkGroups(pages: List<SavedPage>): List<SavedPageGroup> {
        val unfiled = pages.filter { page -> page.folder.isBlank() }
        val folderGroups = pages
            .filter { page -> page.folder.isNotBlank() }
            .groupBy { page -> page.folder }
            .toSortedMap()
            .map { (folder, folderPages) ->
                SavedPageGroup(title = folder, pages = folderPages)
            }
        return listOfNotNull(
            SavedPageGroup(
                title = activity.getString(R.string.bookmark_folder_unfiled),
                pages = unfiled
            ).takeIf { group -> group.pages.isNotEmpty() }
        ) + folderGroups
    }

    private data class SavedPageGroup(
        val title: String,
        val pages: List<SavedPage>
    )
}
