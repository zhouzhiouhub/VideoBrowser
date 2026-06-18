package com.example.videobrowser.localfiles

/**
 * 初学者阅读提示：
 * 这个文件属于“本地文件模块”。
 * 文件名 LocalFilesController 可以拆开理解为“Local Files Controller”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：管理目录授权、读取本地文档列表，并把本地媒体交给浏览器或播放器打开。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.functioncenter.FunctionCenterController
import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.video.ExternalSubtitleCandidate
import com.example.videobrowser.video.PlaybackQueue

/**
 * 本地文件页面控制器。
 *
 * Android 的系统文件选择器只给 Uri，不给普通文件路径。
 * 这个类通过 DocumentsContract 读取目录内容，并把本地媒体文件包装成浏览器或播放器能使用的 Uri。
 */
class LocalFilesController(
    private val activity: AppCompatActivity,
    private val preferenceStore: PreferenceStore,
    private val functionCenter: FunctionCenterController,
    private val logTag: String,
    private val showMainFunctionCenterPage: () -> Unit,
    private val onOpenDocumentUri: (
        Uri,
        String?,
        String?,
        List<ExternalSubtitleCandidate>,
        PlaybackQueue?
    ) -> Unit
) {
    private val directoryPermissions =
        LocalDirectoryPermissionManager(activity, preferenceStore, logTag)
    private val documentRepository = LocalDocumentRepository(activity)
    private val documentFormatter = LocalDocumentFormatter(activity)
    private val fileLaunchers = LocalFileLaunchers(
        activity = activity,
        directoryPermissions = directoryPermissions,
        documentRepository = documentRepository,
        logTag = logTag,
        onOpenDocumentUri = onOpenDocumentUri,
        onDirectoryReady = { uri -> showLocalDirectoryPage(uri) },
        onDirectoryUnavailable = ::showLocalFolderUnavailableToast
    )

    /**
     * 函数 `setupLaunchers`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun setupLaunchers() {
        fileLaunchers.setup()
    }

    /**
     * 函数 `showFileOperationsPage`：控制 `show File Operations Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun showFileOperationsPage() {
        functionCenter.showPage(
            title = activity.getString(R.string.title_file_operations),
            onBack = showMainFunctionCenterPage
        ) { content ->
            functionCenter.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_open_local_file),
                    summary = activity.getString(R.string.action_open_local_file_summary)
                ) {
                    fileLaunchers.openFile()
                }

                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_browse_local_folder),
                    summary = activity.getString(R.string.action_browse_local_folder_summary)
                ) {
                    val uri = directoryPermissions.savedDirectoryUri()
                    if (uri == null) {
                        fileLaunchers.openDirectory(null)
                    } else {
                        showLocalDirectoryPage(uri)
                    }
                }

                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_choose_local_folder),
                    summary = activity.getString(R.string.action_choose_local_folder_summary)
                ) {
                    fileLaunchers.openDirectory(directoryPermissions.savedDirectoryUri())
                }
            }
        }
    }

    /**
     * 函数 `showLocalFolderUnavailableToast`：控制 `show Local Folder Unavailable Toast` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showLocalFolderUnavailableToast() {
        Toast.makeText(activity, R.string.toast_local_folder_unavailable, Toast.LENGTH_SHORT).show()
    }

    /**
     * 函数 `showLocalDirectoryPage`：控制 `show Local Directory Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param treeUri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    private fun showLocalDirectoryPage(treeUri: Uri) {
        val rootDocumentId = documentRepository.rootDocumentId(treeUri)

        if (rootDocumentId == null) {
            directoryPermissions.releaseReadWritePermission(treeUri)
            directoryPermissions.clearSavedDirectoryUri()
            Toast.makeText(activity, R.string.toast_local_folder_unavailable, Toast.LENGTH_SHORT).show()
            showFileOperationsPage()
            return
        }

        showLocalDirectoryPage(
            treeUri = treeUri,
            path = listOf(
                LocalDirectoryPathItem(
                    documentId = rootDocumentId,
                    title = activity.getString(R.string.title_local_files)
                )
            )
        )
    }

    /**
     * 函数 `showLocalDirectoryPage`：控制 `show Local Directory Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param treeUri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param path 参数类型为 `List<LocalDirectoryPathItem>`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     */
    private fun showLocalDirectoryPage(treeUri: Uri, path: List<LocalDirectoryPathItem>) {
        // path 保存从根目录到当前目录的面包屑；进入子目录时追加，返回时 dropLast。
        val current = path.lastOrNull() ?: return showLocalDirectoryPage(treeUri)
        val onBack: () -> Unit = if (path.size > 1) {
            { showLocalDirectoryPage(treeUri, path.dropLast(1)) }
        } else {
            { showFileOperationsPage() }
        }

        val documents = runCatching {
            documentRepository.queryDocuments(treeUri, current.documentId)
        }.getOrElse {
            Log.w(logTag, "Unable to query local directory $treeUri", it)
            directoryPermissions.releaseReadWritePermission(treeUri)
            directoryPermissions.clearSavedDirectoryUri()
            Toast.makeText(
                activity,
                R.string.toast_local_folder_unavailable,
                Toast.LENGTH_SHORT
            ).show()
            showFileOperationsPage()
            return
        }

        functionCenter.showPage(
            title = current.title,
            onBack = onBack
        ) { content ->
            functionCenter.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_new_folder),
                    summary = activity.getString(R.string.action_new_folder_summary)
                ) {
                    promptCreateLocalDocument(
                        treeUri = treeUri,
                        path = path,
                        mimeType = DocumentsContract.Document.MIME_TYPE_DIR,
                        defaultName = activity.getString(R.string.default_new_folder_name),
                        dialogTitle = activity.getString(R.string.title_new_folder)
                    )
                }

                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_new_text_file),
                    summary = activity.getString(R.string.action_new_text_file_summary)
                ) {
                    promptCreateLocalDocument(
                        treeUri = treeUri,
                        path = path,
                        mimeType = "text/plain",
                        defaultName = activity.getString(R.string.default_new_text_file_name),
                        dialogTitle = activity.getString(R.string.title_new_text_file)
                    )
                }

                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_refresh),
                    summary = activity.getString(R.string.action_refresh_local_folder_summary)
                ) {
                    showLocalDirectoryPage(treeUri, path)
                }
            }

            if (documents.isEmpty()) {
                functionCenter.addEmptyState(content, activity.getString(R.string.dialog_local_folder_empty))
                return@showPage
            }

            functionCenter.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_files)
            ) { section ->
                documents.forEach { document ->
                    functionCenter.addActionRow(
                        parent = section,
                        title = document.name,
                        summary = documentFormatter.summary(document)
                    ) {
                        if (document.isDirectory) {
                            showLocalDirectoryPage(
                                treeUri = treeUri,
                                path = path + LocalDirectoryPathItem(document.documentId, document.name)
                            )
                        } else {
                            showLocalDocumentActionsPage(document, treeUri, path, documents)
                        }
                    }
                }
            }
        }
    }

    /**
     * 函数 `showLocalDocumentActionsPage`：控制 `show Local Document Actions Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param document 参数类型为 `LocalDocument`，表示函数执行 `document` 相关逻辑时需要读取或处理的输入。
     * @param treeUri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param path 参数类型为 `List<LocalDirectoryPathItem>`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @param siblingDocuments 参数类型为 `List<LocalDocument>`，表示函数执行 `siblingDocuments` 相关逻辑时需要读取或处理的输入。
     */
    private fun showLocalDocumentActionsPage(
        document: LocalDocument,
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>,
        siblingDocuments: List<LocalDocument>
    ) {
        // 对单个文件提供打开、分享、重命名、删除等动作；媒体文件还会尝试构建同目录播放队列。
        functionCenter.showPage(
            title = document.name,
            onBack = { showLocalDirectoryPage(treeUri, path) }
        ) { content ->
            functionCenter.addFunctionMessage(content, documentFormatter.summary(document))
            functionCenter.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_open_file),
                    summary = activity.getString(R.string.action_open_file_summary)
                ) {
                    // 打开本地媒体时，把同目录媒体和字幕一起传给播放层，播放器就能上一集/下一集和挂字幕。
                    val openRequest = LocalDocumentOpenRequestBuilder.from(document, siblingDocuments)
                    onOpenDocumentUri(
                        document.uri,
                        document.name,
                        document.mimeType,
                        openRequest.subtitleCandidates,
                        openRequest.playbackQueue
                    )
                }

                functionCenter.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_share_file),
                    summary = activity.getString(R.string.action_share_file_summary)
                ) {
                    shareLocalDocument(document)
                }

                if (document.canRename) {
                    functionCenter.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_rename_file),
                        summary = activity.getString(R.string.action_rename_file_summary)
                    ) {
                        promptRenameLocalDocument(document, treeUri, path)
                    }
                }

                if (document.canDelete) {
                    functionCenter.addActionRow(
                        parent = section,
                        title = activity.getString(R.string.action_delete_file),
                        summary = activity.getString(R.string.action_delete_file_summary)
                    ) {
                        confirmDeleteLocalDocument(document, treeUri, path)
                    }
                }
            }
        }
    }

    /**
     * 函数 `promptCreateLocalDocument`：封装 `prompt Create Local Document` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param treeUri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param path 参数类型为 `List<LocalDirectoryPathItem>`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @param mimeType 参数类型为 `String`，表示函数执行 `mimeType` 相关逻辑时需要读取或处理的输入。
     * @param defaultName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param dialogTitle 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    private fun promptCreateLocalDocument(
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>,
        mimeType: String,
        defaultName: String,
        dialogTitle: String
    ) {
        showNameInputDialog(
            title = dialogTitle,
            initialValue = defaultName,
            positiveButtonText = activity.getString(R.string.action_create)
        ) { name ->
            val parent = path.lastOrNull() ?: return@showNameInputDialog
            val createdUri = documentRepository.createDocument(
                treeUri = treeUri,
                parentDocumentId = parent.documentId,
                mimeType = mimeType,
                name = name
            )

            if (createdUri == null) {
                Toast.makeText(activity, R.string.toast_local_file_operation_failed, Toast.LENGTH_SHORT).show()
                return@showNameInputDialog
            }

            Toast.makeText(activity, R.string.toast_local_file_created, Toast.LENGTH_SHORT).show()
            showLocalDirectoryPage(treeUri, path)
        }
    }

    /**
     * 函数 `promptRenameLocalDocument`：封装 `prompt Rename Local Document` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param document 参数类型为 `LocalDocument`，表示函数执行 `document` 相关逻辑时需要读取或处理的输入。
     * @param treeUri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param path 参数类型为 `List<LocalDirectoryPathItem>`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     */
    private fun promptRenameLocalDocument(
        document: LocalDocument,
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>
    ) {
        showNameInputDialog(
            title = activity.getString(R.string.title_rename_file),
            initialValue = document.name,
            positiveButtonText = activity.getString(R.string.action_rename)
        ) { name ->
            val renamedUri = documentRepository.renameDocument(document, name)

            if (renamedUri == null) {
                Toast.makeText(activity, R.string.toast_local_file_operation_failed, Toast.LENGTH_SHORT).show()
                return@showNameInputDialog
            }

            Toast.makeText(activity, R.string.toast_local_file_renamed, Toast.LENGTH_SHORT).show()
            showLocalDirectoryPage(treeUri, path)
        }
    }

    /**
     * 函数 `confirmDeleteLocalDocument`：封装 `confirm Delete Local Document` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param document 参数类型为 `LocalDocument`，表示函数执行 `document` 相关逻辑时需要读取或处理的输入。
     * @param treeUri 参数类型为 `Uri`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param path 参数类型为 `List<LocalDirectoryPathItem>`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     */
    private fun confirmDeleteLocalDocument(
        document: LocalDocument,
        treeUri: Uri,
        path: List<LocalDirectoryPathItem>
    ) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_delete_file)
            .setMessage(activity.getString(R.string.dialog_delete_local_file_message, document.name))
            .setPositiveButton(R.string.action_delete_file) { _, _ ->
                val deleted = documentRepository.deleteDocument(document)

                if (!deleted) {
                    Toast.makeText(
                        activity,
                        R.string.toast_local_file_operation_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                Toast.makeText(activity, R.string.toast_local_file_deleted, Toast.LENGTH_SHORT).show()
                showLocalDirectoryPage(treeUri, path)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showNameInputDialog`：控制 `show Name Input Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param initialValue 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param positiveButtonText 参数类型为 `String`，表示函数执行 `positiveButtonText` 相关逻辑时需要读取或处理的输入。
     * @param onConfirm 参数类型为 `(String) -> Unit`，表示函数执行 `onConfirm` 相关逻辑时需要读取或处理的输入。
     */
    private fun showNameInputDialog(
        title: String,
        initialValue: String,
        positiveButtonText: String,
        onConfirm: (String) -> Unit
    ) {
        val input = EditText(activity).apply {
            setText(initialValue)
            setSelectAllOnFocus(true)
            selectAll()
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }

        AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(input)
            .setPositiveButton(positiveButtonText) { _, _ ->
                val name = input.text?.toString()?.trim().orEmpty()
                if (name.isBlank()) {
                    Toast.makeText(activity, R.string.toast_local_file_name_invalid, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                onConfirm(name)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `shareLocalDocument`：封装 `share Local Document` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param document 参数类型为 `LocalDocument`，表示函数执行 `document` 相关逻辑时需要读取或处理的输入。
     */
    private fun shareLocalDocument(document: LocalDocument) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = document.mimeType?.takeUnless { document.isDirectory } ?: "*/*"
            putExtra(Intent.EXTRA_STREAM, document.uri)
            clipData = ClipData.newUri(activity.contentResolver, document.name, document.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.action_share_file)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
        }
    }
}
