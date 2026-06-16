package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“网页权限请求模块”。
 * 文件名 WebPermissionRequestController 可以拆开理解为“Web Permission Request Controller”，表示它专门负责 WebView 网页请求相机/麦克风权限的流程。
 * 主要职责：把 WebView PermissionRequest 映射到 Android 运行时权限、站点权限设置和用户确认弹窗，并且只授予应用支持的网页资源。
 * 阅读顺序：先看 handlePermissionRequest，再看 handleAndroidPermissionResult 和 showPermissionPrompt，最后看资源映射函数。
 */
import android.Manifest
import android.webkit.PermissionRequest
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.settings.SessionSitePermissionStore
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.settings.SitePermission
import com.example.videobrowser.settings.SitePermissionDecision
import com.example.videobrowser.site.SiteHost

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
    private val activity: AppCompatActivity,
    private val settingsManager: SettingsManager,
    private val sessionSitePermissionStore: SessionSitePermissionStore,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val hasAndroidPermission: (String) -> Boolean,
    private val requestAndroidPermissions: (Array<String>) -> Unit
) {
    private var pendingWebPermissionRequest: PermissionRequest? = null
    private var pendingWebPermissionPromptRequest: PermissionRequest? = null
    private var pendingWebPermissionDialog: AlertDialog? = null

    /**
     * 函数 `handlePermissionRequest`：处理 WebView 发来的网页权限请求。
     *
     * 初学者阅读提示：先过滤不支持的网页资源，再检查站点设置，最后必要时申请 Android 运行时权限。
     *
     * @param request 参数类型为 `PermissionRequest?`，表示 WebView 发来的权限请求；为空时直接忽略。
     */
    fun handlePermissionRequest(request: PermissionRequest?) {
        request ?: return
        val requiredPermissions = androidPermissionsForWebResources(request.resources)
        if (requiredPermissions == null) {
            request.deny()
            return
        }
        if (webPermissionDecision(request) == SitePermissionDecision.BLOCK) {
            request.deny()
            return
        }
        val missingPermissions = requiredPermissions
            .filterNot(hasAndroidPermission)
            .toTypedArray()
        if (missingPermissions.isEmpty()) {
            handlePermissionRequestAfterAndroidPermission(request)
            return
        }

        pendingWebPermissionRequest?.deny()
        cancelPendingPrompt()
        pendingWebPermissionRequest = request
        requestAndroidPermissions(missingPermissions)
    }

    /**
     * 函数 `handleAndroidPermissionResult`：处理 Android 运行时权限申请结果。
     *
     * 初学者阅读提示：只有系统权限全部满足后，才会继续按站点权限设置 grant 或弹出站点确认对话框。
     *
     * @param grants 参数类型为 `Map<String, Boolean>`，表示 Android 权限名到是否授予的映射，由 ActivityResultLauncher 返回。
     */
    fun handleAndroidPermissionResult(grants: Map<String, Boolean>) {
        val request = pendingWebPermissionRequest ?: return
        pendingWebPermissionRequest = null
        val requiredPermissions = androidPermissionsForWebResources(request.resources)
        if (requiredPermissions != null && requiredPermissions.all { permission ->
                grants[permission] == true || hasAndroidPermission(permission)
            }
        ) {
            handlePermissionRequestAfterAndroidPermission(request)
        } else {
            request.deny()
        }
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
            pendingWebPermissionRequest?.deny()
            pendingWebPermissionRequest = null
            cancelPendingPrompt()
            return
        }
        if (request == pendingWebPermissionRequest) {
            pendingWebPermissionRequest = null
        }
        if (request == pendingWebPermissionPromptRequest) {
            cancelPendingPrompt()
        }
    }

    /**
     * 函数 `cancelPendingRequest`：取消当前所有网页权限请求和确认弹窗。
     *
     * 初学者阅读提示：Activity 销毁时调用，避免 WebView 还在等待权限结果。
     */
    fun cancelPendingRequest() {
        pendingWebPermissionRequest?.deny()
        pendingWebPermissionRequest = null
        cancelPendingPrompt()
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
            SitePermissionDecision.ASK -> showPermissionPrompt(request)
        }
    }

    /**
     * 函数 `showPermissionPrompt`：展示网页权限确认弹窗。
     *
     * 初学者阅读提示：用户可以选择永久允许、仅本次允许或拒绝；仅本次允许不会写入持久设置。
     *
     * @param request 参数类型为 `PermissionRequest`，表示需要用户确认的网页权限请求。
     */
    private fun showPermissionPrompt(request: PermissionRequest) {
        cancelPendingPrompt()
        pendingWebPermissionPromptRequest = request
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.title_web_permission_request)
            .setMessage(
                activity.getString(
                    R.string.dialog_web_permission_request_message,
                    webPermissionOrigin(request),
                    webPermissionResourceSummary(request.resources)
                )
            )
            .setPositiveButton(R.string.action_allow) { _, _ ->
                answerPermissionPrompt(request, allowed = true)
            }
            .setNeutralButton(R.string.action_allow_once) { _, _ ->
                answerPermissionPrompt(request, allowed = true, rememberDecision = false)
            }
            .setNegativeButton(R.string.action_deny) { _, _ ->
                answerPermissionPrompt(request, allowed = false)
            }
            .create()
        dialog.setOnCancelListener {
            answerPermissionPrompt(request, allowed = false)
        }
        pendingWebPermissionDialog = dialog
        dialog.show()
    }

    /**
     * 函数 `answerPermissionPrompt`：处理用户在网页权限弹窗中的选择。
     *
     * 初学者阅读提示：允许且记住会写入站点设置；允许但不记住只写入会话权限；拒绝且记住会写入阻止设置。
     *
     * @param request 参数类型为 `PermissionRequest`，表示正在等待用户选择的网页权限请求。
     * @param allowed 参数类型为 `Boolean`，表示用户是否允许这次网页权限请求。
     * @param rememberDecision 参数类型为 `Boolean`，表示是否把用户选择写入持久站点权限设置。
     */
    private fun answerPermissionPrompt(
        request: PermissionRequest,
        allowed: Boolean,
        rememberDecision: Boolean = true
    ) {
        if (pendingWebPermissionPromptRequest != request) {
            return
        }
        pendingWebPermissionPromptRequest = null
        pendingWebPermissionDialog = null
        if (allowed) {
            if (rememberDecision) {
                saveWebPermissionDecision(request, allowed = true)
            } else {
                allowWebPermissionForSession(request)
            }
            grantSupportedWebPermissionResources(request)
        } else {
            if (rememberDecision) {
                saveWebPermissionDecision(request, allowed = false)
            }
            request.deny()
        }
    }

    /**
     * 函数 `cancelPendingPrompt`：关闭当前网页权限确认弹窗并拒绝对应请求。
     *
     * 初学者阅读提示：新的权限请求到来或 Activity 销毁时调用，确保旧请求不会悬挂。
     */
    private fun cancelPendingPrompt() {
        val request = pendingWebPermissionPromptRequest
        pendingWebPermissionPromptRequest = null
        pendingWebPermissionDialog?.dismiss()
        pendingWebPermissionDialog = null
        request?.deny()
    }

    /**
     * 函数 `webPermissionOrigin`：返回网页权限请求来源的展示文案。
     *
     * @param request 参数类型为 `PermissionRequest`，表示网页权限请求，用来读取 origin。
     * @return 返回 origin 字符串；如果请求没有来源，则返回“未知来源”文案。
     */
    private fun webPermissionOrigin(request: PermissionRequest): String {
        return request.origin
            ?.toString()
            ?.takeIf { origin -> origin.isNotBlank() }
            ?: activity.getString(R.string.permission_origin_unknown)
    }

    /**
     * 函数 `webPermissionResourceSummary`：把网页权限资源列表转换成用户可读摘要。
     *
     * @param resources 参数类型为 `Array<String>`，表示 WebView 请求的网页权限资源列表。
     * @return 返回去重后的本地化资源名称，用逗号连接，例如“相机, 麦克风”。
     */
    private fun webPermissionResourceSummary(resources: Array<String>): String {
        return resources
            .mapNotNull { resource -> webPermissionResourceLabel(resource) }
            .distinct()
            .joinToString(", ")
    }

    /**
     * 函数 `webPermissionResourceLabel`：把单个 WebView 权限资源转换成本地化名称。
     *
     * @param resource 参数类型为 `String`，表示 WebView 权限资源常量。
     * @return 返回相机/麦克风名称；未知资源返回 null。
     */
    private fun webPermissionResourceLabel(resource: String): String? {
        return when (resource) {
            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> activity.getString(R.string.web_permission_camera)
            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> activity.getString(R.string.web_permission_microphone)
            else -> null
        }
    }

    /**
     * 函数 `webPermissionDecision`：计算当前网页权限请求的站点权限决策。
     *
     * @param request 参数类型为 `PermissionRequest`，表示要判断的网页权限请求。
     * @return 返回允许、阻止或询问用户的决策。
     */
    private fun webPermissionDecision(request: PermissionRequest): SitePermissionDecision {
        val hostName = SiteHost.fromUrl(request.origin?.toString()) ?: return SitePermissionDecision.ASK
        val permissions = request.resources
            .mapNotNull(::sitePermissionForWebResource)
        val decisions = permissions
            .map { permission -> settingsManager.sitePermissionDecision(hostName, permission) }
        return when {
            decisions.any { decision -> decision == SitePermissionDecision.BLOCK } -> SitePermissionDecision.BLOCK
            permissions.isNotEmpty() &&
                permissions.all { permission ->
                    settingsManager.sitePermissionDecision(hostName, permission) == SitePermissionDecision.ALLOW ||
                        sessionSitePermissionStore.isAllowed(hostName, permission)
                } -> SitePermissionDecision.ALLOW
            else -> SitePermissionDecision.ASK
        }
    }

    /**
     * 函数 `saveWebPermissionDecision`：把用户选择保存到站点权限设置。
     *
     * @param request 参数类型为 `PermissionRequest`，表示用户刚刚处理的网页权限请求。
     * @param allowed 参数类型为 `Boolean`，表示用户是否允许该权限；false 会保存为阻止。
     */
    private fun saveWebPermissionDecision(request: PermissionRequest, allowed: Boolean) {
        if (isPrivateBrowsingEnabled()) {
            if (allowed) {
                allowWebPermissionForSession(request)
            }
            return
        }
        val hostName = SiteHost.fromUrl(request.origin?.toString()) ?: return
        val decision = if (allowed) SitePermissionDecision.ALLOW else SitePermissionDecision.BLOCK
        request.resources
            .mapNotNull(::sitePermissionForWebResource)
            .forEach { permission ->
                settingsManager.setSitePermissionDecision(hostName, permission, decision)
            }
    }

    /**
     * 函数 `allowWebPermissionForSession`：把权限记录为仅本次会话允许。
     *
     * @param request 参数类型为 `PermissionRequest`，表示需要临时允许的网页权限请求。
     */
    private fun allowWebPermissionForSession(request: PermissionRequest) {
        val hostName = SiteHost.fromUrl(request.origin?.toString()) ?: return
        request.resources
            .mapNotNull(::sitePermissionForWebResource)
            .forEach { permission ->
                sessionSitePermissionStore.allow(hostName, permission)
            }
    }

    /**
     * 函数 `sitePermissionForWebResource`：把 WebView 权限资源映射为站点权限类型。
     *
     * @param resource 参数类型为 `String`，表示 WebView 权限资源常量。
     * @return 返回相机或麦克风站点权限；未知资源返回 null。
     */
    private fun sitePermissionForWebResource(resource: String): SitePermission? {
        return when (resource) {
            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> SitePermission.CAMERA
            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> SitePermission.MICROPHONE
            else -> null
        }
    }

    /**
     * 函数 `grantSupportedWebPermissionResources`：只授予应用明确支持的网页权限资源。
     *
     * @param request 参数类型为 `PermissionRequest`，表示要 grant 或 deny 的网页权限请求。
     */
    private fun grantSupportedWebPermissionResources(request: PermissionRequest) {
        val resources = supportedWebPermissionResources(request.resources)
        if (resources == null) {
            request.deny()
            return
        }

        request.grant(resources)
    }

    /**
     * 函数 `supportedWebPermissionResources`：过滤并去重支持的 WebView 权限资源。
     *
     * @param resources 参数类型为 `Array<String>`，表示 WebView 原始请求资源列表。
     * @return 返回只包含相机/麦克风的资源数组；如果包含未知资源则返回 null 让调用方拒绝。
     */
    private fun supportedWebPermissionResources(resources: Array<String>): Array<String>? {
        val supportedResources = mutableListOf<String>()
        resources.forEach { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                    if (resource !in supportedResources) {
                        supportedResources += resource
                    }
                }

                else -> return null
            }
        }
        return supportedResources.toTypedArray().takeIf { it.isNotEmpty() }
    }

    /**
     * 函数 `androidPermissionsForWebResources`：把 WebView 权限资源映射为 Android 运行时权限。
     *
     * @param resources 参数类型为 `Array<String>`，表示 WebView 请求的网页权限资源列表。
     * @return 返回需要申请的 Android 权限列表；如果包含未知网页资源则返回 null。
     */
    private fun androidPermissionsForWebResources(resources: Array<String>): List<String>? {
        val permissions = mutableListOf<String>()
        resources.forEach { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> permissions += Manifest.permission.CAMERA
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> permissions += Manifest.permission.RECORD_AUDIO
                else -> return null
            }
        }
        return permissions.takeIf { it.isNotEmpty() }
    }
}
