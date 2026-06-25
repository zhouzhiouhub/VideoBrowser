package com.example.videobrowser.functioncenter

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Saved Pages Page Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedPagesPageContractTest {
    @Test
    fun savedPageCollectionDisplayTextOwnsCollectionLabels() {
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesDialogController.kt"
        ).readText()
        val displayText = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageCollectionDisplayText.kt"
        ).readText()

        assertTrue(pages.contains("private fun showSavedPagesCollection(collection: SavedPageCollection)"))
        assertTrue(pages.contains("SavedPageCollectionDisplayText.title(activity, collection)"))
        assertTrue(pages.contains("SavedPageCollectionDisplayText.emptyMessage(activity, collection)"))
        assertTrue(dialogController.contains("SavedPageCollectionDisplayText.title(activity, collection)"))
        assertTrue(dialogController.contains("SavedPageCollectionDisplayText.emptyMessage(activity, collection)"))

        assertTrue(displayText.contains("internal object SavedPageCollectionDisplayText"))
        assertTrue(displayText.contains("SavedPageCollection.BOOKMARKS -> context.getString(R.string.title_bookmarks)"))
        assertTrue(displayText.contains("SavedPageCollection.HISTORY -> context.getString(R.string.title_history)"))
        assertTrue(
            displayText.contains(
                "SavedPageCollection.BOOKMARKS -> context.getString(R.string.toast_bookmarks_empty)"
            )
        )
        assertTrue(
            displayText.contains(
                "SavedPageCollection.HISTORY -> context.getString(R.string.toast_history_empty)"
            )
        )

        assertFalse(dialogController.contains("private fun collectionTitle("))
        assertFalse(dialogController.contains("private fun collectionEmptyMessage("))
    }

    /**
     * 测试函数 `savedPagesPageSupportsSearchFiltering`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `saved Pages Page Supports Search Filtering` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun savedPagesPageSupportsSearchFiltering() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesDialogController.kt"
        ).readText()

        assertTrue(page.contains("SavedPageSearch.filter(allPages, query)"))
        assertTrue(page.contains("R.string.action_search_saved_pages"))
        assertTrue(page.contains("dialogController.showSearchDialog(collection, title, emptyMessage, query)"))
        assertTrue(dialogController.contains("fun showSearchDialog"))
        assertTrue(page.contains("R.string.action_clear_search"))
        assertTrue(page.contains("R.string.dialog_saved_pages_search_empty"))
    }

    /**
     * 测试函数 `savedPagesPageCanCopyRecordLinks`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `saved Pages Page Can Copy Record Links` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun savedPagesPageCanCopyRecordLinks() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesDialogController.kt"
        ).readText()
        val inlineActions = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageInlineActionController.kt"
        ).readText()
        val linkActions = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageLinkActions.kt"
        ).readText()

        assertTrue(page.contains("SavedPagesDialogController("))
        assertTrue(page.contains("SavedPageInlineActionController("))
        assertTrue(inlineActions.contains("R.string.action_copy_link"))
        assertTrue(inlineActions.contains("linkActions.copyUrl(page)"))
        assertTrue(linkActions.contains("PageUrlActions.copyPageUrl(activity, page.url)"))
        assertFalse(dialogController.contains("ActionListDialog.show("))
        assertFalse(linkActions.contains("ClipData.newPlainText"))
        assertFalse(linkActions.contains("Context.CLIPBOARD_SERVICE"))
    }

    /**
     * 测试函数 `savedPagesPageCanShareRecordLinks`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `saved Pages Page Can Share Record Links` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun savedPagesPageCanShareRecordLinks() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesDialogController.kt"
        ).readText()
        val inlineActions = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageInlineActionController.kt"
        ).readText()
        val linkActions = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageLinkActions.kt"
        ).readText()

        assertTrue(inlineActions.contains("R.string.action_share_page"))
        assertTrue(inlineActions.contains("linkActions.shareUrl(page)"))
        assertTrue(linkActions.contains("PageUrlActions.sharePageUrl(activity, page.url)"))
        assertFalse(dialogController.contains("ActionListDialog.show("))
        assertFalse(linkActions.contains("Intent(Intent.ACTION_SEND)"))
        assertFalse(linkActions.contains("putExtra(Intent.EXTRA_TEXT"))
    }

    /**
     * 测试函数 `savedPagesPageCanOpenRecordsInNewTabs`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `saved Pages Page Can Open Records In New Tabs` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun savedPagesPageCanOpenRecordsInNewTabs() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val functionCenterAssembly = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterAssemblyController.kt"
        )
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(page.contains("openUrlInNewTab: (String) -> Unit"))
        val inlineActions = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageInlineActionController.kt"
        ).readText()
        assertTrue(inlineActions.contains("R.string.action_open_in_new_tab"))
        assertTrue(inlineActions.contains("openUrlInNewTab(page.url)"))
        assertTrue(pages.contains("openUrlInNewTab = openUrlInNewTab"))
        assertTrue(functionCenterAssembly.contains("openUrlInNewTab = browserTabActionsController::openUrlInNewTab"))
        assertTrue(strings.contains("action_open_in_new_tab"))
    }

    @Test
    fun savedPagesPageOpensRowsAndExpandsInlineActionsOnLongPress() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()
        val recordSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageRecordSection.kt"
        ).readText()
        val rowFactory = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRowFactory.kt"
        ).readText()
        val contentFactory = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterContentFactory.kt"
        ).readText()
        val inlineActions = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageInlineActionController.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesDialogController.kt"
        ).readText()

        assertTrue(page.contains("private val recordSection = SavedPageRecordSection("))
        assertTrue(page.contains("expandedUrl: String? = null"))
        assertTrue(page.contains("recordSection.add("))
        assertTrue(recordSection.contains("onClick = { openPage(page) }"))
        assertTrue(recordSection.contains("onLongClick = {"))
        assertTrue(recordSection.contains("showExpandedPage("))
        assertTrue(recordSection.contains("inlineActionController.addActions("))
        assertTrue(contentFactory.contains("onLongClick: (() -> Unit)? = null"))
        assertTrue(rowFactory.contains("setOnLongClickListener"))
        assertTrue(inlineActions.contains("savedPageRepository.remove(collection, page.url)"))
        assertTrue(inlineActions.contains("ShortToast.show(activity, R.string.toast_saved_page_removed)"))
        assertFalse(dialogController.contains("showSavedPageActionsDialog"))
        assertFalse(dialogController.contains("ActionListDialog.show("))
    }

    @Test
    fun historyRecordsUseDedicatedCompactTimelineStyle() {
        val recordSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageRecordSection.kt"
        ).readText()
        val historySection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageHistoryRecordSection.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(recordSection.contains("private val historyRecordSection = SavedPageHistoryRecordSection("))
        assertTrue(recordSection.contains("historyRecordSection.add("))
        assertTrue(historySection.contains("R.string.history_filter_all"))
        assertTrue(historySection.contains("private fun addDateChip("))
        assertTrue(historySection.contains("SimpleDateFormat(\"yyyy-MM-dd E\", Locale.CHINA)"))
        assertTrue(historySection.contains("SimpleDateFormat(\"HH:mm\", Locale.CHINA)"))
        assertTrue(historySection.contains("private fun createSelectedMarker()"))
        assertTrue(historySection.contains("if (selected) {"))
        assertTrue(historySection.contains("inlineActionController.addActions("))
        assertTrue(strings.contains("history_filter_all"))
        assertTrue(strings.contains("history_date_unknown"))
    }

    @Test
    fun historyLongPressOnlyShowsCopyAndRemoveActions() {
        val inlineActions = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageInlineActionController.kt"
        ).readText()
        val actionStrip = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageHistorySelectionActionStrip.kt"
        ).readText()
        val historyBranch = historyBranchBody(inlineActions)

        assertTrue(historyBranch.contains("addHistorySelectionActions(section, collection, page, title, emptyMessage, query)"))
        assertTrue(inlineActions.contains("SavedPageHistorySelectionActionStrip(host)"))
        assertTrue(inlineActions.contains("historySelectionActionStrip.add("))
        assertTrue(actionStrip.contains("private fun createButton("))
        assertTrue(actionStrip.contains("R.string.action_copy_link"))
        assertTrue(actionStrip.contains("R.string.action_remove"))
        assertTrue(inlineActions.contains("linkActions.copyUrl(page)"))
        assertTrue(inlineActions.contains("removePage(collection, page, title, emptyMessage, query)"))
        assertFalse(historyBranch.contains("showRenameSavedPageDialog"))
        assertFalse(historyBranch.contains("action_open_in_new_tab"))
        assertFalse(historyBranch.contains("action_share_page"))
        assertFalse(historyBranch.contains("action_move_bookmark_folder"))
    }

    /**
     * 测试函数 `savedPagesPageCanRenameBookmarks`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `saved Pages Page Can Rename Bookmarks` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun savedPagesPageCanRenameBookmarks() {
        val inlineActions = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageInlineActionController.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesDialogController.kt"
        ).readText()
        val repository = projectFile(
            "src/main/java/com/example/videobrowser/storage/SavedPageRepository.kt"
        ).readText()
        val codec = projectFile(
            "src/main/java/com/example/videobrowser/storage/SavedPageCodec.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(repository.contains("fun updateTitle(collection: SavedPageCollection, url: String, title: String): Boolean"))
        assertTrue(inlineActions.contains("dialogController.showRenameSavedPageDialog(collection, page, title, emptyMessage, query)"))
        assertTrue(dialogController.contains("fun showRenameSavedPageDialog("))
        assertTrue(dialogController.contains("savedPageRepository.updateTitle("))
        assertTrue(dialogController.contains("collection = collection"))
        assertTrue(dialogController.contains("R.string.title_rename_saved_page"))
        assertTrue(strings.contains("title_rename_saved_page"))
        assertTrue(strings.contains("hint_saved_page_title"))
        assertTrue(strings.contains("toast_saved_page_renamed"))
        assertTrue(strings.contains("toast_saved_page_title_invalid"))
    }

    /**
     * 测试函数 `savedPagesPageCanGroupAndMoveBookmarksByFolder`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `saved Pages Page Can Group And Move Bookmarks By Folder` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun savedPagesPageCanGroupAndMoveBookmarksByFolder() {
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesDialogController.kt"
        ).readText()
        val repository = projectFile(
            "src/main/java/com/example/videobrowser/storage/SavedPageRepository.kt"
        ).readText()
        val codec = projectFile(
            "src/main/java/com/example/videobrowser/storage/SavedPageCodec.kt"
        ).readText()
        val models = projectFile(
            "src/main/java/com/example/videobrowser/storage/SavedPageModels.kt"
        ).readText()
        val search = projectFile("src/main/java/com/example/videobrowser/storage/SavedPageSearch.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()
        val inlineActions = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageInlineActionController.kt"
        ).readText()
        val recordSection = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPageRecordSection.kt"
        ).readText()

        assertTrue(models.contains("val folder: String = \"\""))
        assertTrue(repository.contains("fun updateBookmarkFolder(url: String, folder: String): Boolean"))
        assertTrue(repository.contains("fun bookmarkFolders(): List<String>"))
        assertTrue(codec.contains("VideoBrowserSavedPages\\t3"))
        assertTrue(recordSection.contains("private fun bookmarkGroups"))
        assertTrue(recordSection.contains("R.string.bookmark_folder_unfiled"))
        assertTrue(recordSection.contains("R.string.bookmark_folder_count"))
        assertTrue(recordSection.contains("R.string.bookmark_folder_summary"))
        assertTrue(inlineActions.contains("R.string.action_move_bookmark_folder"))
        assertTrue(inlineActions.contains("collection == SavedPageCollection.BOOKMARKS"))
        assertTrue(dialogController.contains("fun showMoveBookmarkFolderDialog"))
        assertTrue(dialogController.contains("savedPageRepository.updateBookmarkFolder("))
        assertTrue(search.contains("\${page.title}\\n\${page.url}\\n\${page.folder}"))
        assertTrue(strings.contains("action_move_bookmark_folder"))
        assertTrue(strings.contains("action_move_bookmark_folder_summary"))
        assertTrue(strings.contains("title_move_bookmark_folder"))
        assertTrue(strings.contains("hint_bookmark_folder"))
        assertTrue(strings.contains("toast_bookmark_folder_updated"))
        assertTrue(readme.contains("历史记录按日期分组显示，长按会标注选中记录"))
        assertTrue(readme.contains("收藏夹支持文件夹分组、导入和导出"))
    }

    private fun historyBranchBody(source: String): String {
        val marker = "if (collection == SavedPageCollection.HISTORY)"
        val start = source.indexOf(marker)
        assertTrue(start >= 0)
        val bodyStart = source.indexOf('{', start)
        assertTrue(bodyStart >= 0)
        var depth = 0
        for (index in bodyStart until source.length) {
            when (source[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) {
                        return source.substring(bodyStart, index + 1)
                    }
                }
            }
        }
        error("Unclosed history branch")
    }

}
