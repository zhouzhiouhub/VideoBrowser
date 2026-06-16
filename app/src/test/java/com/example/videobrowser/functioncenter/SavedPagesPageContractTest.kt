package com.example.videobrowser.functioncenter

/**
 * 测试阅读提示：
 * 这个测试文件验证“Saved Pages Page Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedPagesPageContractTest {
    @Test
    fun savedPagesPageSupportsSearchFiltering() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()

        assertTrue(page.contains("SavedPageSearch.filter(allPages, query)"))
        assertTrue(page.contains("R.string.action_search_saved_pages"))
        assertTrue(page.contains("private fun showSearchDialog"))
        assertTrue(page.contains("R.string.action_clear_search"))
        assertTrue(page.contains("R.string.dialog_saved_pages_search_empty"))
    }

    @Test
    fun savedPagesPageCanCopyRecordLinks() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()

        assertTrue(page.contains("private fun savedPageActions"))
        assertTrue(page.contains("R.string.action_copy_link"))
        assertTrue(page.contains("copySavedPageUrl(page)"))
        assertTrue(page.contains("ClipData.newPlainText"))
        assertTrue(page.contains("Context.CLIPBOARD_SERVICE"))
        assertTrue(page.contains("R.string.clipboard_page_url"))
        assertTrue(page.contains("R.string.toast_link_copied"))
    }

    @Test
    fun savedPagesPageCanShareRecordLinks() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()

        assertTrue(page.contains("R.string.action_share_page"))
        assertTrue(page.contains("shareSavedPageUrl(page)"))
        assertTrue(page.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(page.contains("type = \"text/plain\""))
        assertTrue(page.contains("putExtra(Intent.EXTRA_TEXT, page.url)"))
        assertTrue(page.contains("Intent.createChooser(intent, activity.getString(R.string.action_share_page))"))
    }

    @Test
    fun savedPagesPageCanOpenRecordsInNewTabs() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()
        val pages = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt"
        ).readText()
        val mainActivity = projectFile("src/main/java/com/example/videobrowser/MainActivity.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(page.contains("openUrlInNewTab: (String) -> Unit"))
        assertTrue(page.contains("R.string.action_open_in_new_tab"))
        assertTrue(page.contains("openUrlInNewTab(page.url)"))
        assertTrue(pages.contains("openUrlInNewTab = openUrlInNewTab"))
        assertTrue(mainActivity.contains("openUrlInNewTab = ::openUrlInNewTab"))
        assertTrue(strings.contains("action_open_in_new_tab"))
    }

    @Test
    fun savedPagesPageCanRenameBookmarks() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()
        val repository = projectFile(
            "src/main/java/com/example/videobrowser/storage/SavedPageRepository.kt"
        ).readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(repository.contains("fun updateTitle(collection: SavedPageCollection, url: String, title: String): Boolean"))
        assertTrue(page.contains("collection == SavedPageCollection.BOOKMARKS"))
        assertTrue(page.contains("private fun showRenameBookmarkDialog"))
        assertTrue(page.contains("savedPageRepository.updateTitle("))
        assertTrue(page.contains("R.string.title_rename_bookmark"))
        assertTrue(strings.contains("title_rename_bookmark"))
        assertTrue(strings.contains("hint_saved_page_title"))
        assertTrue(strings.contains("toast_saved_page_renamed"))
        assertTrue(strings.contains("toast_saved_page_title_invalid"))
    }

    @Test
    fun savedPagesPageCanGroupAndMoveBookmarksByFolder() {
        val page = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/SavedPagesPage.kt"
        ).readText()
        val repository = projectFile(
            "src/main/java/com/example/videobrowser/storage/SavedPageRepository.kt"
        ).readText()
        val search = projectFile("src/main/java/com/example/videobrowser/storage/SavedPageSearch.kt")
            .readText()
        val strings = projectFile("src/main/res/values/strings.xml").readText()
        val readme = projectFile("README.md").readText()

        assertTrue(repository.contains("val folder: String = \"\""))
        assertTrue(repository.contains("fun updateBookmarkFolder(url: String, folder: String): Boolean"))
        assertTrue(repository.contains("fun bookmarkFolders(): List<String>"))
        assertTrue(repository.contains("VideoBrowserSavedPages\\t3"))
        assertTrue(page.contains("private fun bookmarkGroups"))
        assertTrue(page.contains("R.string.bookmark_folder_unfiled"))
        assertTrue(page.contains("R.string.bookmark_folder_count"))
        assertTrue(page.contains("R.string.bookmark_folder_summary"))
        assertTrue(page.contains("R.string.action_move_bookmark_folder"))
        assertTrue(page.contains("private fun showMoveBookmarkFolderDialog"))
        assertTrue(page.contains("savedPageRepository.updateBookmarkFolder("))
        assertTrue(search.contains("\${page.title}\\n\${page.url}\\n\${page.folder}"))
        assertTrue(strings.contains("action_move_bookmark_folder"))
        assertTrue(strings.contains("title_move_bookmark_folder"))
        assertTrue(strings.contains("hint_bookmark_folder"))
        assertTrue(strings.contains("toast_bookmark_folder_updated"))
        assertTrue(readme.contains("收藏夹支持重命名标题、文件夹分组、导入和导出"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
