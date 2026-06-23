package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“网页权限请求模块”。
 * 文件名 WebPermissionRequestController 可以拆开理解为“Web Permission Request Controller”，表示它专门负责 WebView 网页请求相机/麦克风权限的流程。
 * 主要职责：把 WebView PermissionRequest 映射到 Android 运行时权限、站点权限设置和用户确认弹窗，并且只授予应用支持的网页资源。
 * 阅读顺序：先看 handlePermissionRequest，再看 handleAndroidPermissionResult 和 WebPermissionPromptController，最后看资源映射函数。
 */
import android.webkit.PermissionRequest
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.settings.SessionSitePermissionStore
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.settings.SitePermissionDecision

/**
 * WebView 相机/麦克风权限请求控制器。
 *
 * MainActivity 负责注册 Android 权限 launcher，本类负责持有待处理请求、显示站点权限弹窗和决定 grant/deny。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来创建权限说明对话框并读取字符串资源。
 * @param settingsManager 参数类型为 `SettingsManager`，表示持久化站点权限设置入口，用来读取和保存允许/阻止决策。
 * @param sessionSitePermissionStore 参数类型为 `SessionSitePermissionStore`，表示仅本次会话允许的站点权限存储，无痕或“仅本次允许”会写入这里。
 * @param isPrivateBrowsingEnabled 参数类型为 `() -> Boolean`，表示当前是否处于无痕模式；无痕模式下不会持久保存允许决策。
 * @param hasAndroidPermission 参数类型为 `(String) -> Boolean`，表示检查 Android 运行时权限是否已经授予的回调。
 * @param requestAndroidPermissions 参数类型为 `(Array<String>) -> Unit`，表示启动 Android 多权限申请的回调，参数是缺失的系统权限列表。
 */
class WebPermissionRequestController(
    activity: AppCompatActivity,
    settingsManager: SettingsManager,
    sessionSitePermissionStore: SessionSitePermissionStore,
    isPrivateBrowsingEnabled: () -> Boolean,
    hasAndroidPermission: (String) -> Boolean,
    requestAndroidPermissions: (Array<String>) -> Unit
) {
    private val pendingRequestStore = WebPermissionPendingRequestStore()
    private val sitePermissionDecisionController = BrowserSitePermissionDecisionController(
        settingsManager = settingsManager,
        sessionSitePermissionStore = sessionSitePermissionStore,
        isPrivateBrowsingEnabled = isPrivateBrowsingEnabled
    )
    private val webPermissionPromptController = WebPermissionPromptController(
        activity = activity,
        saveDecision = ::saveWebPermissionDecision,
        allowForSession = ::allowWebPermissionForSession,
        grantSupportedResources = ::grantSupportedWebPermissionResources
    )
    private val androidPermissionGate = BrowserAndroidPermissionGate(
        hasAndroidPermission = hasAndroidPermission,
        requestAndroidPermissions = requestAndroidPermissions,
        requiredPermissionsFor = { request: PermissionRequest ->
            WebPermissionResourceMapper.androidPermissionsFor(request.resources)?.toTypedArray()
        },
        resultPolicy = BrowserAndroidPermissionResultPolicy.ALL_REQUIRED,
        replacePendingRequest = { request ->
            pendingRequestStore.replaceWith(request)
            webPermissionPromptController.cancelPendingPrompt()
        },
        takePendingRequest = pendingRequestStore::take,
        continueAfterPermission = ::handlePermissionRequestAfterAndroidPermission,
        denyRequest = PermissionRequest::deny
    )

    /**
     * 函数 `handlePermissionRequest`：处理 WebView 发来的网页权限请求。
     *
     * 初学者阅读提示：先过滤不支持的网页资源，再检查站点设置，最后必要时申请 Android 运行时权限。
     *
     * @param request 参数类型为 `PermissionRequest?`，表示 WebView 发来的权限请求；为空时直接忽略。
     */
    fun handlePermissionRequest(request: PermissionRequest?) {
        request ?: return
        if (webPermissionDecision(request) == SitePermissionDecision.BLOCK) {
            request.deny()
            return
        }
        androidPermissionGate.continueOrRequest(request)
    }

    /**
     * 函数 `handleAndroidPermissionResult`：处理 Android 运行时权限申请结果。
     *
     * 初学者阅读提示：只有系统权限全部满足后，才会继续按站点权限设置 grant 或弹出站点确认对话框。
     *
     * @param grants 参数类型为 `Map<String, Boolean>`，表示 Android 权限名到是否授予的映射，由 ActivityResultLauncher 返回。
     */
    fun handleAndroidPermissionResult(grants: Map<String, Boolean>) {
        androidPermissionGate.handleResult(grants)
    }

    /**
     * 函数 `handlePermissionRequestCanceled`：处理 WebView 取消网页权限请求的回调。
     *
     * 初学者阅读提示：如果 WebView 没有传具体 request，就取消当前所有待处理网页权限流程。
     *
     * @param request 参数类型为 `PermissionRequest?`，表示被 WebView 取消的权限请求；为空表示取消当前 pending 请求。
     */
    fun handlePermissionRequestCanceled(request: PermissionRequest?) {
        if (request == null) {
            cancelAllPendingPermissionFlows()
            return
        }
        pendingRequestStore.clearIfPending(request)
        webPermissionPromptController.cancelIfPending(request)
    }

    /**
     * 函数 `cancelPendingRequest`：取消当前所有网页权限请求和确认弹窗。
     *
     * 初学者阅读提示：Activity 销毁时调用，避免 WebView 还在等待权限结果。
     */
    fun cancelPendingRequest() {
        cancelAllPendingPermissionFlows()
    }

    private fun cancelAllPendingPermissionFlows() {
        pendingRequestStore.cancelPending()
        webPermissionPromptController.cancelPendingPrompt()
    }

    /**
     * 函数 `handlePermissionRequestAfterAndroidPermission`：在 Android 系统权限满足后处理站点权限决策。
     *
     * 初学者阅读提示：持久允许会直接 grant，持久阻止会 deny，未知时再显示站点确认弹窗。
     *
     * @param request 参数类型为 `PermissionRequest`，表示已经通过系统权限检查的网页权限请求。
     */
    private fun handlePermissionRequestAfterAndroidPermission(request: PermissionRequest) {
        when (webPermissionDecision(request)) {
            SitePermissionDecision.ALLOW -> grantSupportedWebPermissionResources(request)
            SitePermissionDecision.BLOCK -> request.deny()
            SitePermissionDecision.ASK -> webPermissionPromptController.show(request)
        }
    }

    /**
     * 函数 `webPermissionDecision`：计算当前网页权限请求的站点权限决策。
     *
     * @param request 参数类型为 `PermissionRequest`，表示要判断的网页权限请求。
     * @return 返回允许、阻止或询问用户的决策。
     */
    private fun webPermissionDecision(request: PermissionRequest): SitePermissionDecision {
        return sitePermissionDecisionController.decisionForOrigin(
            origin = request.origin?.toString(),
            permissions = WebPermissionResourceMapper.sitePermissionsFor(request.resources)
        )
    }

    /**
     * 函数 `saveWebPermissionDecision`：把用户选择保存到站点权限设置。
     *
     * @param request 参数类型为 `PermissionRequest`，表示用户刚刚处理的网页权限请求。
     * @param allowed 参数类型为 `Boolean`，表示用户是否允许该权限；false 会保存为阻止。
     */
    private fun saveWebPermissionDecision(request: PermissionRequest, allowed: Boolean) {
        sitePermissionDecisionController.saveDecisionForOrigin(
            origin = request.origin?.toString(),
            permissions = WebPermissionResourceMapper.sitePermissionsFor(request.resources),
            allowed = allowed
        )
    }

    /**
     * 函数 `allowWebPermissionForSession`：把权限记录为仅本次会话允许。
     *
     * @param request 参数类型为 `PermissionRequest`，表示需要临时允许的网页权限请求。
     */
    private fun allowWebPermissionForSession(request: PermissionRequest) {
        sitePermissionDecisionController.allowForSession(
            origin = request.origin?.toString(),
            permissions = WebPermissionResourceMapper.sitePermissionsFor(request.resources)
        )
    }

    /**
     * 函数 `grantSupportedWebPermissionResources`：只授予应用明确支持的网页权限资源。
     *
     * @param request 参数类型为 `PermissionRequest`，表示要 grant 或 deny 的网页权限请求。
     */
    private fun grantSupportedWebPermissionResources(request: PermissionRequest) {
        val resources = WebPermissionResourceMapper.supportedResources(request.resources)
        if (resources == null) {
            request.deny()
            return
        }

        request.grant(resources)
    }
}
