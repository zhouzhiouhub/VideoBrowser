package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 Activity Result 模块”。
 * 文件名 BrowserActivityResultLaunchers 可以拆开理解为“Browser Activity Result Launchers”，
 * 表示它只负责注册 Android 系统选择器和权限申请入口，并把返回结果转交给对应业务控制器。
 * 阅读顺序：先看构造参数理解每个结果会交给哪个控制器，再看公开的 launch/request 方法。
 */
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.storage.BookmarkImportExportController

/**
 * 浏览器 Activity Result 启动器集合。
 *
 * MainActivity 仍然决定何时创建各业务控制器；本类只集中管理 Android 系统 Activity Result API 的注册、
 * 启动和结果分发。构造参数使用可空 provider，是为了避免系统回调早于业务控制器初始化时发生 lateinit 访问错误。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示注册 Activity Result launcher 的宿主 Activity。
 * @param webFileChooserController 参数类型为 `() -> WebFileChooserController?`，表示返回网页文件选择控制器的函数，尚未初始化时返回 null。
 * @param bookmarkImportExportController 参数类型为 `() -> BookmarkImportExportController?`，表示返回收藏夹导入导出控制器的函数，尚未初始化时返回 null。
 * @param pageArchiveController 参数类型为 `() -> PageArchiveController?`，表示返回网页归档控制器的函数，尚未初始化时返回 null。
 * @param webPermissionRequestController 参数类型为 `() -> WebPermissionRequestController?`，表示返回网页相机/麦克风权限控制器的函数，尚未初始化时返回 null。
 * @param geolocationPermissionController 参数类型为 `() -> GeolocationPermissionController?`，表示返回网页定位权限控制器的函数，尚未初始化时返回 null。
 */
class BrowserActivityResultLaunchers(
    activity: AppCompatActivity,
    private val webFileChooserController: () -> WebFileChooserController?,
    private val bookmarkImportExportController: () -> BookmarkImportExportController?,
    private val pageArchiveController: () -> PageArchiveController?,
    private val webPermissionRequestController: () -> WebPermissionRequestController?,
    private val geolocationPermissionController: () -> GeolocationPermissionController?
) {
    private val webFileChooserLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            webFileChooserController()?.handleActivityResult(result.resultCode, result.data)
        }
    private val bookmarkExportLauncher =
        activity.registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            if (uri != null) {
                bookmarkImportExportController()?.exportToUri(uri)
            }
        }
    private val bookmarkImportLauncher =
        activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                bookmarkImportExportController()?.importFromUri(uri)
            }
        }
    private val pageArchiveExportLauncher =
        activity.registerForActivityResult(ActivityResultContracts.CreateDocument(PageArchiveController.MIME_TYPE)) { uri ->
            pageArchiveController()?.handleExportResult(uri)
        }
    private val webPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            webPermissionRequestController()?.handleAndroidPermissionResult(grants)
        }
    private val geolocationPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            geolocationPermissionController()?.handleAndroidPermissionResult(grants)
        }

    /**
     * 启动网页文件选择器。
     *
     * @param intent 参数类型为 `Intent`，表示 WebView 文件上传流程创建好的 chooser Intent。
     * @return 无返回值；函数会把 intent 交给系统 Activity Result launcher。
     */
    fun launchWebFileChooser(intent: Intent) {
        webFileChooserLauncher.launch(intent)
    }

    /**
     * 启动收藏夹导出文件创建器。
     *
     * @return 无返回值；函数会使用固定文件名打开系统文档创建器。
     */
    fun launchBookmarkExport() {
        bookmarkExportLauncher.launch(BookmarkImportExportController.EXPORT_FILE_NAME)
    }

    /**
     * 启动收藏夹导入文件选择器。
     *
     * @return 无返回值；函数会使用收藏夹导入允许的 MIME 类型打开系统文档选择器。
     */
    fun launchBookmarkImport() {
        bookmarkImportLauncher.launch(BookmarkImportExportController.IMPORT_MIME_TYPES)
    }

    /**
     * 启动网页归档导出文件创建器。
     *
     * @param fileName 参数类型为 `String`，表示页面归档控制器建议给系统文档创建器的文件名。
     * @return 无返回值；函数会把建议文件名交给系统 Activity Result launcher。
     */
    fun launchPageArchiveExport(fileName: String) {
        pageArchiveExportLauncher.launch(fileName)
    }

    /**
     * 申请网页相机或麦克风所需的 Android 运行时权限。
     *
     * @param permissions 参数类型为 `Array<String>`，表示网页权限控制器计算出的缺失 Android 权限列表。
     * @return 无返回值；函数会触发系统多权限申请弹窗。
     */
    fun requestWebPermissions(permissions: Array<String>) {
        webPermissionLauncher.launch(permissions)
    }

    /**
     * 申请网页定位所需的 Android 运行时权限。
     *
     * @param permissions 参数类型为 `Array<String>`，表示网页定位控制器需要申请的精确或大致定位权限列表。
     * @return 无返回值；函数会触发系统多权限申请弹窗。
     */
    fun requestGeolocationPermissions(permissions: Array<String>) {
        geolocationPermissionLauncher.launch(permissions)
    }
}
