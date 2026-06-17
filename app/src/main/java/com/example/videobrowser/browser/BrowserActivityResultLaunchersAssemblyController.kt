package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器 Activity Result 装配模块”。
 * 文件名 BrowserActivityResultLaunchersAssemblyController 可以拆开理解为“Browser Activity Result Launchers Assembly Controller”，
 * 表示它只负责创建 Android 系统文件选择、文档导入导出和权限申请的 launcher 集合。
 * 阅读顺序：先看 create() 如何把可空 provider 传给 BrowserActivityResultLaunchers，再去 BrowserActivityResultLaunchers 看每个 launcher 的具体用途。
 */
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.storage.BookmarkImportExportController

/**
 * Activity Result launcher 装配控制器。
 *
 * 系统 Activity Result 回调可能在某些业务控制器初始化之前到达；本类保留可空 provider，
 * 让 BrowserActivityResultLaunchers 可以安全地把结果转交给已经准备好的控制器。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示注册 Activity Result launcher 的宿主 Activity。
 * @param webFileChooserController 参数类型为 `() -> WebFileChooserController?`，表示安全读取网页文件选择控制器的回调。
 * @param bookmarkImportExportController 参数类型为 `() -> BookmarkImportExportController?`，表示安全读取收藏夹导入导出控制器的回调。
 * @param pageArchiveController 参数类型为 `() -> PageArchiveController?`，表示安全读取网页归档控制器的回调。
 * @param webPermissionRequestController 参数类型为 `() -> WebPermissionRequestController?`，表示安全读取网页相机/麦克风权限控制器的回调。
 * @param geolocationPermissionController 参数类型为 `() -> GeolocationPermissionController?`，表示安全读取网页定位权限控制器的回调。
 */
class BrowserActivityResultLaunchersAssemblyController(
    private val activity: AppCompatActivity,
    private val webFileChooserController: () -> WebFileChooserController?,
    private val bookmarkImportExportController: () -> BookmarkImportExportController?,
    private val pageArchiveController: () -> PageArchiveController?,
    private val webPermissionRequestController: () -> WebPermissionRequestController?,
    private val geolocationPermissionController: () -> GeolocationPermissionController?
) {
    /**
     * 创建 Activity Result launcher 集合。
     *
     * @return 返回 `BrowserActivityResultLaunchers`，调用方保存后交给文件选择、导入导出、网页权限和归档控制器使用。
     */
    fun create(): BrowserActivityResultLaunchers {
        return BrowserActivityResultLaunchers(
            activity = activity,
            webFileChooserController = webFileChooserController,
            bookmarkImportExportController = bookmarkImportExportController,
            pageArchiveController = pageArchiveController,
            webPermissionRequestController = webPermissionRequestController,
            geolocationPermissionController = geolocationPermissionController
        )
    }
}
