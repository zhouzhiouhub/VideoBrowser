package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“WebView 外部请求装配模块”。
 * 文件名 BrowserWebRequestAssemblyController 可以拆开理解为“Browser Web Request Assembly Controller”，
 * 表示它只负责创建 WebView 文件选择、网页相机/麦克风权限和网页定位权限相关控制器。
 * 阅读顺序：先看 BrowserWebRequestComponents 知道 MainActivity 会拿到哪些对象，再看 create() 中各控制器如何共享权限检查器。
 */
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.settings.SessionSitePermissionStore
import com.example.videobrowser.settings.SettingsManager

/**
 * WebView 外部请求组件集合。
 *
 * @param webFileChooserController 参数类型为 `WebFileChooserController`，表示 WebView 文件上传选择器控制器。
 * @param webPermissionRequestController 参数类型为 `WebPermissionRequestController`，表示 WebView 相机/麦克风权限请求控制器。
 * @param geolocationPermissionController 参数类型为 `GeolocationPermissionController`，表示 WebView 定位权限请求控制器。
 */
data class BrowserWebRequestComponents(
    val webFileChooserController: WebFileChooserController,
    val webPermissionRequestController: WebPermissionRequestController,
    val geolocationPermissionController: GeolocationPermissionController
)

/**
 * WebView 外部请求装配控制器。
 *
 * WebChromeClient 会把文件选择、网页权限和网页定位回调交给这些控制器；本类集中创建它们，
 * 让 MainActivity 不再保存只在权限装配时使用的 AndroidPermissionChecker。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示创建文件选择器、权限弹窗和读取资源的宿主 Activity。
 * @param settingsManager 参数类型为 `SettingsManager`，表示读取和写入站点权限持久设置的数据源。
 * @param sessionSitePermissionStore 参数类型为 `SessionSitePermissionStore`，表示保存“仅本次允许”站点权限的会话级存储。
 * @param browserFeatureStateController 参数类型为 `BrowserFeatureStateController`，表示提供当前是否处于无痕浏览的控制器。
 * @param activityResultLaunchers 参数类型为 `BrowserActivityResultLaunchers`，表示启动系统文件选择器和 Android 运行时权限申请的 launcher 集合。
 */
class BrowserWebRequestAssemblyController(
    private val activity: AppCompatActivity,
    private val settingsManager: SettingsManager,
    private val sessionSitePermissionStore: SessionSitePermissionStore,
    private val browserFeatureStateController: BrowserFeatureStateController,
    private val activityResultLaunchers: BrowserActivityResultLaunchers
) {
    /**
     * 创建 WebView 外部请求组件集合。
     *
     * @return 返回 `BrowserWebRequestComponents`，调用方把其中对象保存到 MainActivity 字段后交给 BrowserChromeClientController 使用。
     */
    fun create(): BrowserWebRequestComponents {
        val androidPermissionChecker = AndroidPermissionChecker(activity)
        val webFileChooserController = WebFileChooserController(
            activity = activity,
            launchChooser = activityResultLaunchers::launchWebFileChooser
        )
        val webPermissionRequestController = WebPermissionRequestController(
            activity = activity,
            settingsManager = settingsManager,
            sessionSitePermissionStore = sessionSitePermissionStore,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            hasAndroidPermission = androidPermissionChecker::hasAndroidPermission,
            requestAndroidPermissions = activityResultLaunchers::requestWebPermissions
        )
        val geolocationPermissionController = GeolocationPermissionController(
            activity = activity,
            settingsManager = settingsManager,
            sessionSitePermissionStore = sessionSitePermissionStore,
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            hasAndroidPermission = androidPermissionChecker::hasAndroidPermission,
            requestAndroidPermissions = activityResultLaunchers::requestGeolocationPermissions
        )
        return BrowserWebRequestComponents(
            webFileChooserController = webFileChooserController,
            webPermissionRequestController = webPermissionRequestController,
            geolocationPermissionController = geolocationPermissionController
        )
    }
}
