package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 SavedPagesPage 可以拆开理解为“Saved Pages Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.storage.SavedPageRepository.SavedPageCollection
import com.example.videobrowser.storage.SavedPageSearch

class SavedPagesPage(
    private val host: FunctionCenterPageHost,
    private val savedPageRepository: SavedPageRepository,
    private val openUrlInNewTab: (String) -> Unit,
    private val loadUrl: (String) -> Unit,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity
    private val linkActions = SavedPageLinkActions(activity)
    private val dialogController = SavedPagesDialogController(
        activity = activity,
        savedPageRepository = savedPageRepository,
        showSavedPagesPage = { collection, title, emptyMessage, replaceCurrent, query ->
            show(
                collection = collection,
                title = title,
                emptyMessage = emptyMessage,
                replaceCurrent = replaceCurrent,
                query = query
            )
        }
    )
    private val historyPageController = SavedPageHistoryPageController(
        host = host,
        savedPageRepository = savedPageRepository,
        dialogController = dialogController,
        loadUrl = loadUrl,
        showRootPage = showRootPage,
        showHistoryPage = { title, emptyMessage, replaceCurrent, query ->
            show(
                collection = SavedPageCollection.HISTORY,
                title = title,
                emptyMessage = emptyMessage,
                replaceCurrent = replaceCurrent,
                query = query
            )
        }
    )
    private val inlineActionController = SavedPageInlineActionController(
        host = host,
        savedPageRepository = savedPageRepository,
        dialogController = dialogController,
        linkActions = linkActions,
        openUrlInNewTab = openUrlInNewTab,
        refreshSavedPagesPage = { collection, title, emptyMessage, query ->
            show(
                collection = collection,
                title = title,
                emptyMessage = emptyMessage,
                replaceCurrent = true,
                query = query
            )
        }
    )
    private val recordSection = SavedPageRecordSection(
        host = host,
        inlineActionController = inlineActionController,
        openPage = { page -> loadUrl(page.url) },
        showExpandedPage = { collection, title, emptyMessage, query, expandedUrl ->
            show(
                collection = collection,
                title = title,
                emptyMessage = emptyMessage,
                replaceCurrent = true,
                query = query,
                expandedUrl = expandedUrl
            )
        }
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
        query: String? = null,
        expandedUrl: String? = null
    ) {
        val allPages = savedPageRepository.pages(collection)
        val pages = SavedPageSearch.filter(allPages, query)

        if (collection == SavedPageCollection.HISTORY) {
            historyPageController.show(
                allPages = allPages,
                pages = pages,
                title = title,
                emptyMessage = emptyMessage,
                replaceCurrent = replaceCurrent,
                query = query
            )
            return
        }
        historyPageController.reset()

        host.showPage(
            title = title,
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (allPages.isEmpty()) {
                host.contentFactory.addEmptyState(content, emptyMessage)
                return@showPage
            }

            host.contentFactory.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.contentFactory.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_search_saved_pages),
                    summary = SearchSummaryFormatter.current(
                        query,
                        activity.getString(R.string.action_search_saved_pages_summary)
                    )
                ) {
                    dialogController.showSearchDialog(collection, title, emptyMessage, query)
                }
                if (!query.isNullOrBlank()) {
                    host.contentFactory.addActionRow(
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
                host.contentFactory.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_saved_pages_summary)
                ) {
                    dialogController.showClearSavedPagesDialog(collection)
                }
            }

            host.contentFactory.addFunctionSection(
                parent = content,
                title = activity.getString(R.string.function_center_section_records)
            ) { section ->
                if (pages.isEmpty()) {
                    host.contentFactory.addEmptyState(section, activity.getString(R.string.dialog_saved_pages_search_empty))
                    return@addFunctionSection
                }
                recordSection.add(
                    section = section,
                    collection = collection,
                    pages = pages,
                    title = title,
                    emptyMessage = emptyMessage,
                    query = query,
                    expandedUrl = expandedUrl
                )
            }
        }
    }

}
