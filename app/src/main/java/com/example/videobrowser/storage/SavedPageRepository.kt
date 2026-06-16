package com.example.videobrowser.storage

/**
 * 初学者阅读提示：
 * 这个文件属于“收藏与历史存储模块”。
 * 文件名 SavedPageRepository 可以拆开理解为“Saved Page Repository”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：读写收藏夹、浏览历史、导入导出数据，并提供搜索和过滤能力。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.Locale

data class SavedPage(
    val title: String,
    val url: String,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L,
    val folder: String = ""
)

data class BookmarkImportResult(
    val importedCount: Int,
    val skippedCount: Int
)

/**
 * 收藏夹和浏览历史仓库。
 *
 * 数据保存在 PreferenceStore 中，仓库负责去重、限制数量、导入导出、旧格式兼容和 URL 规范化。
 */
class SavedPageRepository(
    private val preferenceStore: PreferenceStore,
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() }
) {
    /**
     * 函数 `addBookmark`：封装 `add Bookmark` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param page 参数类型为 `SavedPage`，表示函数执行 `page` 相关逻辑时需要读取或处理的输入。
     */
    fun addBookmark(page: SavedPage) {
        addSavedPage(KEY_BOOKMARKS, page, BOOKMARK_LIMIT)
    }

    /**
     * 函数 `removeBookmark`：封装 `remove Bookmark` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    fun removeBookmark(url: String) {
        removeSavedPage(KEY_BOOKMARKS, url)
    }

    /**
     * 函数 `isBookmarked`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isBookmarked(url: String): Boolean {
        return isSavedPage(KEY_BOOKMARKS, url)
    }

    /**
     * 函数 `addHistory`：封装 `add History` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param page 参数类型为 `SavedPage`，表示函数执行 `page` 相关逻辑时需要读取或处理的输入。
     */
    fun addHistory(page: SavedPage) {
        addSavedPage(KEY_HISTORY, page, HISTORY_LIMIT)
    }

    /**
     * 函数 `bookmarks`：封装 `bookmarks` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun bookmarks(): List<SavedPage> {
        return loadSavedPages(KEY_BOOKMARKS)
    }

    /**
     * 函数 `exportBookmarks`：封装 `export Bookmarks` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun exportBookmarks(): String {
        return renderPages(bookmarks())
    }

    /**
     * 函数 `bookmarkFolders`：封装 `bookmark Folders` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun bookmarkFolders(): List<String> {
        return bookmarks()
            .mapNotNull { page -> normalizeBookmarkFolder(page.folder) }
            .distinct()
            .sorted()
    }

    /**
     * 函数 `importBookmarks`：封装 `import Bookmarks` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rawValue 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun importBookmarks(rawValue: String): BookmarkImportResult {
        // 导入时会过滤无效 URL、跳过已存在链接，并尊重收藏数量上限。
        val existingBookmarks = bookmarks()
        val existingUrls = existingBookmarks
            .map { page -> bookmarkUrlKey(page.url) }
            .toSet()
        val candidates = parseSavedPages(rawValue)
            .mapNotNull(::normalizeImportedBookmark)
            .distinctBy { page -> bookmarkUrlKey(page.url) }
        val availableSlots = (BOOKMARK_LIMIT - existingBookmarks.size).coerceAtLeast(0)
        val newBookmarks = candidates
            .filterNot { page -> bookmarkUrlKey(page.url) in existingUrls }
            .take(availableSlots)

        if (newBookmarks.isNotEmpty()) {
            saveSavedPages(KEY_BOOKMARKS, existingBookmarks + newBookmarks)
        }
        return BookmarkImportResult(
            importedCount = newBookmarks.size,
            skippedCount = candidates.size - newBookmarks.size
        )
    }

    /**
     * 函数 `history`：封装 `history` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun history(): List<SavedPage> {
        return loadSavedPages(KEY_HISTORY)
    }

    /**
     * 函数 `clearHistory`：封装 `clear History` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clearHistory() {
        preferenceStore.remove(KEY_HISTORY)
    }

    /**
     * 函数 `clearHistoryUpdatedSince`：封装 `clear History Updated Since` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param cutoffMillis 参数类型为 `Long`，表示函数执行 `cutoffMillis` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun clearHistoryUpdatedSince(cutoffMillis: Long): Int {
        val history = history()
        val remainingHistory = history.filterNot { page ->
            page.updatedAtMillis >= cutoffMillis
        }
        saveSavedPages(KEY_HISTORY, remainingHistory)
        return history.size - remainingHistory.size
    }

    /**
     * 函数 `clear`：封装 `clear` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param collection 参数类型为 `SavedPageCollection`，表示函数执行 `collection` 相关逻辑时需要读取或处理的输入。
     */
    fun clear(collection: SavedPageCollection) {
        preferenceStore.remove(collection.key)
    }

    /**
     * 函数 `remove`：封装 `remove` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param collection 参数类型为 `SavedPageCollection`，表示函数执行 `collection` 相关逻辑时需要读取或处理的输入。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun remove(collection: SavedPageCollection, url: String): Boolean {
        return removeSavedPage(collection.key, url)
    }

    /**
     * 函数 `updateTitle`：根据最新状态刷新 `update Title` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param collection 参数类型为 `SavedPageCollection`，表示函数执行 `collection` 相关逻辑时需要读取或处理的输入。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun updateTitle(collection: SavedPageCollection, url: String, title: String): Boolean {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) {
            return false
        }
        var updated = false
        val pages = loadSavedPages(collection.key).map { page ->
            if (page.url.equals(url, ignoreCase = true)) {
                updated = true
                page.copy(title = normalizedTitle, updatedAtMillis = currentTimeMillis())
            } else {
                page
            }
        }
        if (!updated) {
            return false
        }
        saveSavedPages(collection.key, pages)
        return true
    }

    /**
     * 函数 `updateBookmarkFolder`：根据最新状态刷新 `update Bookmark Folder` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param folder 参数类型为 `String`，表示函数执行 `folder` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun updateBookmarkFolder(url: String, folder: String): Boolean {
        val collapsedFolder = folder.trim().replace(Regex("\\s+"), " ")
        val normalizedFolder = if (collapsedFolder.isEmpty()) {
            ""
        } else {
            normalizeBookmarkFolder(collapsedFolder) ?: return false
        }
        var updated = false
        val pages = bookmarks().map { page ->
            if (page.url.equals(url, ignoreCase = true)) {
                updated = true
                page.copy(folder = normalizedFolder, updatedAtMillis = currentTimeMillis())
            } else {
                page
            }
        }
        if (!updated) {
            return false
        }
        saveSavedPages(KEY_BOOKMARKS, pages)
        return true
    }

    /**
     * 函数 `clearAll`：封装 `clear All` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clearAll() {
        SavedPageCollection.values().forEach { collection ->
            preferenceStore.remove(collection.key)
        }
    }

    /**
     * 函数 `pages`：封装 `pages` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param collection 参数类型为 `SavedPageCollection`，表示函数执行 `collection` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun pages(collection: SavedPageCollection): List<SavedPage> {
        return loadSavedPages(collection.key)
    }

    /**
     * 函数 `addSavedPage`：封装 `add Saved Page` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param page 参数类型为 `SavedPage`，表示函数执行 `page` 相关逻辑时需要读取或处理的输入。
     * @param limit 参数类型为 `Int`，表示函数执行 `limit` 相关逻辑时需要读取或处理的输入。
     */
    private fun addSavedPage(key: String, page: SavedPage, limit: Int) {
        val normalizedUrl = normalizeSavedWebUrl(page.url) ?: return
        val pageToSave = page.copy(url = normalizedUrl)
        val existingPages = loadSavedPages(key)
        val existingPage = existingPages.firstOrNull { it.url.equals(normalizedUrl, ignoreCase = true) }
        val pages = existingPages
            .filterNot { it.url.equals(normalizedUrl, ignoreCase = true) }
            .toMutableList()
        pages.add(0, normalizePageForSave(pageToSave, existingPage))
        saveSavedPages(key, pages.take(limit))
    }

    /**
     * 函数 `removeSavedPage`：封装 `remove Saved Page` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun removeSavedPage(key: String, url: String): Boolean {
        val pages = loadSavedPages(key)
            .filterNot { it.url.equals(url, ignoreCase = true) }
        if (pages.size == loadSavedPages(key).size) {
            return false
        }
        saveSavedPages(key, pages)
        return true
    }

    /**
     * 函数 `isSavedPage`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isSavedPage(key: String, url: String): Boolean {
        return loadSavedPages(key).any { it.url.equals(url, ignoreCase = true) }
    }

    /**
     * 函数 `loadSavedPages`：启动或加载 `load Saved Pages` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun loadSavedPages(key: String): List<SavedPage> {
        val rawValue = preferenceStore.getString(key, null) ?: return emptyList()
        return parseSavedPages(rawValue)
    }

    /**
     * 函数 `parseSavedPages`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rawValue 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun parseSavedPages(rawValue: String): List<SavedPage> {
        // 新格式是带版本头的行文本；旧版本曾用 JSON，这里保留读取兼容。
        return when {
            rawValue.startsWith(FORMAT_HEADER_PREFIX) -> loadVersionedPages(rawValue)
            rawValue.trimStart().startsWith("[") -> loadLegacyJsonPages(rawValue)
            else -> emptyList()
        }
    }

    /**
     * 函数 `saveSavedPages`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param pages 参数类型为 `List<SavedPage>`，表示函数执行 `pages` 相关逻辑时需要读取或处理的输入。
     */
    private fun saveSavedPages(key: String, pages: List<SavedPage>) {
        if (pages.isEmpty()) {
            preferenceStore.remove(key)
            return
        }
        preferenceStore.putString(key, renderPages(pages))
    }

    /**
     * 函数 `normalizePageForSave`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param page 参数类型为 `SavedPage`，表示函数执行 `page` 相关逻辑时需要读取或处理的输入。
     * @param existingPage 参数类型为 `SavedPage?`，表示函数执行 `existingPage` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizePageForSave(page: SavedPage, existingPage: SavedPage?): SavedPage {
        val timestamp = currentTimeMillis()
        val createdAt = existingPage?.createdAtMillis
            ?.takeIf { it > 0L }
            ?: page.createdAtMillis.takeIf { it > 0L }
            ?: timestamp
        val updatedAt = page.updatedAtMillis.takeIf { it > 0L } ?: timestamp
        return page.copy(
            title = page.title.trim(),
            url = page.url.trim(),
            createdAtMillis = createdAt,
            updatedAtMillis = updatedAt,
            folder = normalizeBookmarkFolder(page.folder).orEmpty()
        )
    }

    /**
     * 函数 `normalizeImportedBookmark`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param page 参数类型为 `SavedPage`，表示函数执行 `page` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeImportedBookmark(page: SavedPage): SavedPage? {
        val url = normalizeSavedWebUrl(page.url) ?: return null
        val timestamp = currentTimeMillis()
        return page.copy(
            title = page.title.trim().ifBlank { url },
            url = url,
            createdAtMillis = page.createdAtMillis.takeIf { it > 0L } ?: timestamp,
            updatedAtMillis = page.updatedAtMillis.takeIf { it > 0L } ?: timestamp,
            folder = normalizeBookmarkFolder(page.folder).orEmpty()
        )
    }

    /**
     * 函数 `bookmarkUrlKey`：封装 `bookmark Url Key` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun bookmarkUrlKey(url: String): String {
        return url.trim().lowercase(Locale.ROOT)
    }

    /**
     * 函数 `normalizeBookmarkFolder`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param folder 参数类型为 `String`，表示函数执行 `folder` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeBookmarkFolder(folder: String): String? {
        val normalized = folder.trim().replace(Regex("\\s+"), " ")
        if (normalized.isEmpty()) {
            return null
        }
        if (normalized.length > MAX_BOOKMARK_FOLDER_LENGTH) {
            return null
        }
        if (normalized.any { char -> char == '\t' || char == '\n' || char == '\r' }) {
            return null
        }
        return normalized
    }

    /**
     * 函数 `renderPages`：封装 `render Pages` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pages 参数类型为 `List<SavedPage>`，表示函数执行 `pages` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun renderPages(pages: List<SavedPage>): String {
        return buildString {
            append(FORMAT_HEADER).append('\n')
            pages.forEach { page ->
                append(page.createdAtMillis.coerceAtLeast(0L))
                    .append('\t')
                    .append(page.updatedAtMillis.coerceAtLeast(0L))
                    .append('\t')
                    .append(encode(page.title))
                    .append('\t')
                    .append(encode(page.url))
                    .append('\t')
                    .append(encode(page.folder))
                    .append('\n')
            }
        }
    }

    /**
     * 函数 `loadVersionedPages`：启动或加载 `load Versioned Pages` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rawValue 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun loadVersionedPages(rawValue: String): List<SavedPage> {
        return rawValue
            .lineSequence()
            .drop(1)
            .mapNotNull(::parseVersionedPageLine)
            .filter { page -> page.url.isNotBlank() }
            .toList()
    }

    /**
     * 函数 `parseVersionedPageLine`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param line 参数类型为 `String`，表示函数执行 `line` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun parseVersionedPageLine(line: String): SavedPage? {
        val parts = line.split('\t')
        if (parts.size != 4 && parts.size != 5) {
            return null
        }
        val createdAt = parts[0].toLongOrNull() ?: 0L
        val updatedAt = parts[1].toLongOrNull() ?: createdAt
        val title = decode(parts[2]) ?: return null
        val url = decode(parts[3])?.takeIf { it.isNotBlank() } ?: return null
        val normalizedUrl = normalizeSavedWebUrl(url) ?: return null
        val folder = parts.getOrNull(4)?.let(::decode)?.let(::normalizeBookmarkFolder).orEmpty()
        return SavedPage(
            title = title,
            url = normalizedUrl,
            createdAtMillis = createdAt,
            updatedAtMillis = updatedAt,
            folder = folder
        )
    }

    /**
     * 函数 `loadLegacyJsonPages`：启动或加载 `load Legacy Json Pages` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rawValue 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun loadLegacyJsonPages(rawValue: String): List<SavedPage> {
        val timestamp = currentTimeMillis()
        return LEGACY_OBJECT_REGEX.findAll(rawValue)
            .mapNotNull { match ->
                val objectText = match.value
                val url = legacyJsonStringValue(objectText, JSON_URL)
                    ?.let(::normalizeSavedWebUrl)
                    ?: return@mapNotNull null
                SavedPage(
                    title = legacyJsonStringValue(objectText, JSON_TITLE).orEmpty(),
                    url = url,
                    createdAtMillis = timestamp,
                    updatedAtMillis = timestamp
                )
            }
            .toList()
    }

    /**
     * 函数 `legacyJsonStringValue`：封装 `legacy Json String Value` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param objectText 参数类型为 `String`，表示函数执行 `objectText` 相关逻辑时需要读取或处理的输入。
     * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun legacyJsonStringValue(objectText: String, key: String): String? {
        val match = Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"")
            .find(objectText)
            ?: return null
        return unescapeJsonString(match.groupValues[1])
    }

    /**
     * 函数 `unescapeJsonString`：封装 `unescape Json String` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun unescapeJsonString(value: String): String {
        val builder = StringBuilder()
        var index = 0
        while (index < value.length) {
            val char = value[index]
            if (char != '\\' || index == value.lastIndex) {
                builder.append(char)
                index += 1
                continue
            }
            when (val escaped = value[index + 1]) {
                '"', '\\', '/' -> builder.append(escaped)
                'b' -> builder.append('\b')
                'f' -> builder.append('\u000C')
                'n' -> builder.append('\n')
                'r' -> builder.append('\r')
                't' -> builder.append('\t')
                'u' -> {
                    val hex = value.substring(index + 2, (index + 6).coerceAtMost(value.length))
                    val codePoint = hex.takeIf { it.length == 4 }?.toIntOrNull(16)
                    if (codePoint != null) {
                        builder.append(codePoint.toChar())
                        index += 4
                    }
                }
                else -> builder.append(escaped)
            }
            index += 2
        }
        return builder.toString()
    }

    /**
     * 函数 `encode`：封装 `encode` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun encode(value: String): String {
        return URLEncoder.encode(value, CHARSET_NAME)
    }

    /**
     * 函数 `decode`：封装 `decode` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun decode(value: String): String? {
        return runCatching { URLDecoder.decode(value, CHARSET_NAME) }.getOrNull()
    }

    /**
     * 函数 `normalizeSavedWebUrl`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeSavedWebUrl(url: String): String? {
        val normalizedUrl = url.trim().takeIf { it.isNotBlank() } ?: return null
        val uri = runCatching { URI(normalizedUrl) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return null
        if (scheme != "http" && scheme != "https") {
            return null
        }
        if (uri.host.isNullOrBlank()) {
            return null
        }
        return normalizedUrl
    }

    enum class SavedPageCollection(val key: String) {
        BOOKMARKS(KEY_BOOKMARKS),
        HISTORY(KEY_HISTORY)
    }

    private companion object {
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_HISTORY = "history"
        private const val FORMAT_HEADER = "VideoBrowserSavedPages\t3"
        private const val FORMAT_HEADER_PREFIX = "VideoBrowserSavedPages\t"
        private const val JSON_TITLE = "title"
        private const val JSON_URL = "url"
        private const val CHARSET_NAME = "UTF-8"
        private const val BOOKMARK_LIMIT = 500
        private const val MAX_BOOKMARK_FOLDER_LENGTH = 60
        private const val HISTORY_LIMIT = 1000
        private val LEGACY_OBJECT_REGEX = Regex("\\{[^{}]*\\}")
    }
}
