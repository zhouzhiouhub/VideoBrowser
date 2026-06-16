package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“网页地理位置权限模块”。
 * 文件名 GeolocationPermissionController 可以拆开理解为“Geolocation Permission Controller”，表示它专门负责 WebView 网页定位权限请求。
 * 主要职责：把 WebView 地理位置回调连接到 Android 运行时定位权限、站点权限设置、仅本次允许和用户确认弹窗。
 * 阅读顺序：先看 handlePermissionRequest，再看 handleAndroidPermissionResult，最后看权限决策和保存函数。
 */
import android.Manifest
import android.webkit.GeolocationPermissions
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.settings.SessionSitePermissionStore
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.settings.SitePermission
import com.example.videobrowser.settings.SitePermissionDecision
import com.example.videobrowser.site.SiteHost

/**
 * WebView 地理位置权限控制器。
 *
 * MainActivity 负责注册 Android 权限 launcher，本类负责保存待处理 WebView 回调、显示站点权限弹窗和回复网页。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来创建权限说明对话框并读取字符串资源。
 * @param settingsManager 参数类型为 `SettingsManager`，表示持久化站点权限设置入口，用来读取和保存定位权限决策。
 * @param sessionSitePermissionStore 参数类型为 `SessionSitePermissionStore`，表示仅本次会话允许的站点权限存储，无痕或“仅本次允许”会写入这里。
 * @param isPrivateBrowsingEnabled 参数类型为 `() -> Boolean`，表示当前是否处于无痕模式；无痕模式下不会持久保存允许决策。
 * @param hasAndroidPermission 参数类型为 `(String) -> Boolean`，表示检查 Android 运行时定位权限是否已经授予的回调。
 * @param requestAndroidPermissions 参数类型为 `(Array<String>) -> Unit`，表示启动 Android 多权限申请的回调，参数是精确和大致定位权限。
 */
class GeolocationPermissionController(
    private val activity: AppCompatActivity,
    private val settingsManager: SettingsManager,
    private val sessionSitePermissionStore: SessionSitePermissionStore,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val hasAndroidPermission: (String) -> Boolean,
    private val requestAndroidPermissions: (Array<String>) -> Unit
) {
    private var pendingPermissionPrompt: GeolocationPermissionPrompt? = null
    private var pendingSitePrompt: GeolocationPermissionPrompt? = null
    private var pendingDialog: AlertDialog? = null

    /**
     * 函数 `handlePermissionRequest`：处理 WebView 发来的地理位置权限请求。
     *
     * 初学者阅读提示：先看站点是否已阻止，再看 Android 定位权限是否已满足，最后才申请系统权限或显示站点确认弹窗。
     *
     * @param origin 参数类型为 `String?`，表示网页请求定位权限的来源地址，可能为空。
     * @param callback 参数类型为 `GeolocationPermissions.Callback?`，表示 WebView 等待定位权限结果的回调；为空时直接忽略。
     */
    fun handlePermissionRequest(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        callback ?: return
        val siteDecision = geolocationPermissionDecision(origin)
        if (siteDecision == SitePermissionDecision.BLOCK) {
            denyPermissionPrompt(origin, callback)
            return
        }
        val permissions = androidLocationPermissions()
        if (permissions.any(hasAndroidPermission)) {
            handleAfterAndroidPermission(
                GeolocationPermissionPrompt(origin, callback)
            )
            return
        }

        cancelPending()
        pendingPermissionPrompt = GeolocationPermissionPrompt(origin, callback)
        requestAndroidPermissions(permissions)
    }

    /**
     * 函数 `handleAndroidPermissionResult`：处理 Android 定位运行时权限申请结果。
     *
     * 初学者阅读提示：精确或大致定位任意一个被授予即可继续站点权限流程，否则拒绝网页定位请求。
     *
     * @param grants 参数类型为 `Map<String, Boolean>`，表示 Android 权限名到是否授予的映射，由 ActivityResultLauncher 返回。
     */
    fun handleAndroidPermissionResult(grants: Map<String, Boolean>) {
        val prompt = pendingPermissionPrompt ?: return
        pendingPermissionPrompt = null
        val allowed = androidLocationPermissions().any { permission ->
            grants[permission] == true || hasAndroidPermission(permission)
        }
        if (allowed) {
            handleAfterAndroidPermission(prompt)
        } else {
            denyPermissionPrompt(prompt.origin, prompt.callback)
        }
    }

    /**
     * 函数 `handlePermissionHidden`：处理 WebView 隐藏定位权限提示的回调。
     *
     * 初学者阅读提示：网页主动取消或页面离开时调用，内部会拒绝所有仍在等待的定位请求。
     */
    fun handlePermissionHidden() {
        cancelPending()
    }

    /**
     * 函数 `cancelPending`：取消当前定位权限流程并拒绝待处理 WebView 回调。
     *
     * 初学者阅读提示：Activity 销毁、网页隐藏提示或新请求到来时调用，确保旧请求不会悬挂。
     */
    fun cancelPending() {
        val prompt = pendingPermissionPrompt
        pendingPermissionPrompt = null
        prompt?.let { pendingPrompt ->
            denyPermissionPrompt(pendingPrompt.origin, pendingPrompt.callback)
        }

        val sitePrompt = pendingSitePrompt
        pendingSitePrompt = null
        pendingDialog?.dismiss()
        pendingDialog = null
        sitePrompt?.let { pendingPrompt ->
            denyPermissionPrompt(pendingPrompt.origin, pendingPrompt.callback)
        }
    }

    /**
     * 函数 `handleAfterAndroidPermission`：在 Android 定位权限满足后处理站点定位权限决策。
     *
     * @param prompt 参数类型为 `GeolocationPermissionPrompt`，表示已经通过系统权限检查、等待站点权限决策的请求。
     */
    private fun handleAfterAndroidPermission(prompt: GeolocationPermissionPrompt) {
        when (geolocationPermissionDecision(prompt.origin)) {
            SitePermissionDecision.ALLOW -> prompt.callback.invoke(prompt.origin, true, false)
            SitePermissionDecision.BLOCK -> denyPermissionPrompt(prompt.origin, prompt.callback)
            SitePermissionDecision.ASK -> showPermissionPrompt(prompt)
        }
    }

    /**
     * 函数 `showPermissionPrompt`：展示网页定位权限确认弹窗。
     *
     * 初学者阅读提示：用户可以选择永久允许、仅本次允许或拒绝；仅本次允许不会写入持久站点设置。
     *
     * @param prompt 参数类型为 `GeolocationPermissionPrompt`，表示需要用户确认的网页定位权限请求。
     */
    private fun showPermissionPrompt(prompt: GeolocationPermissionPrompt) {
        cancelPending()
        pendingSitePrompt = prompt
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.title_geolocation_permission_request)
            .setMessage(
                activity.getString(
                    R.string.dialog_geolocation_permission_request_message,
                    prompt.origin?.takeIf { origin -> origin.isNotBlank() }
                        ?: activity.getString(R.string.permission_origin_unknown)
                )
            )
            .setPositiveButton(R.string.action_allow) { _, _ ->
                answerPermissionPrompt(prompt, allowed = true)
            }
            .setNeutralButton(R.string.action_allow_once) { _, _ ->
                answerPermissionPrompt(prompt, allowed = true, rememberDecision = false)
            }
            .setNegativeButton(R.string.action_deny) { _, _ ->
                answerPermissionPrompt(prompt, allowed = false)
            }
            .create()
        dialog.setOnCancelListener {
            answerPermissionPrompt(prompt, allowed = false)
        }
        pendingDialog = dialog
        dialog.show()
    }

    /**
     * 函数 `answerPermissionPrompt`：处理用户在网页定位权限弹窗中的选择。
     *
     * @param prompt 参数类型为 `GeolocationPermissionPrompt`，表示正在等待用户选择的定位权限请求。
     * @param allowed 参数类型为 `Boolean`，表示用户是否允许这次网页定位请求。
     * @param rememberDecision 参数类型为 `Boolean`，表示是否把用户选择写入持久站点权限设置。
     */
    private fun answerPermissionPrompt(
        prompt: GeolocationPermissionPrompt,
        allowed: Boolean,
        rememberDecision: Boolean = true
    ) {
        if (pendingSitePrompt != prompt) {
            return
        }
        pendingSitePrompt = null
        pendingDialog = null
        if (allowed) {
            if (rememberDecision) {
                saveGeolocationPermissionDecision(prompt.origin, allowed = true)
            } else {
                allowGeolocationPermissionForSession(prompt.origin)
            }
        } else if (rememberDecision) {
            saveGeolocationPermissionDecision(prompt.origin, allowed = false)
        }
        prompt.callback.invoke(prompt.origin, allowed, false)
    }

    /**
     * 函数 `geolocationPermissionDecision`：计算当前网页定位请求的站点权限决策。
     *
     * @param origin 参数类型为 `String?`，表示网页请求定位权限的来源地址。
     * @return 返回允许、阻止或询问用户的决策。
     */
    private fun geolocationPermissionDecision(origin: String?): SitePermissionDecision {
        val hostName = SiteHost.fromUrl(origin) ?: return SitePermissionDecision.ASK
        val decision = settingsManager.sitePermissionDecision(hostName, SitePermission.LOCATION)
        return when {
            decision == SitePermissionDecision.BLOCK -> SitePermissionDecision.BLOCK
            decision == SitePermissionDecision.ALLOW ||
                sessionSitePermissionStore.isAllowed(hostName, SitePermission.LOCATION) -> SitePermissionDecision.ALLOW
            else -> SitePermissionDecision.ASK
        }
    }

    /**
     * 函数 `saveGeolocationPermissionDecision`：把用户选择保存到站点权限设置。
     *
     * @param origin 参数类型为 `String?`，表示网页请求定位权限的来源地址。
     * @param allowed 参数类型为 `Boolean`，表示用户是否允许定位；false 会保存为阻止。
     */
    private fun saveGeolocationPermissionDecision(origin: String?, allowed: Boolean) {
        if (isPrivateBrowsingEnabled()) {
            if (allowed) {
                allowGeolocationPermissionForSession(origin)
            }
            return
        }
        val hostName = SiteHost.fromUrl(origin) ?: return
        settingsManager.setSitePermissionDecision(
            host = hostName,
            permission = SitePermission.LOCATION,
            decision = if (allowed) SitePermissionDecision.ALLOW else SitePermissionDecision.BLOCK
        )
    }

    /**
     * 函数 `allowGeolocationPermissionForSession`：把定位权限记录为仅本次会话允许。
     *
     * @param origin 参数类型为 `String?`，表示网页请求定位权限的来源地址。
     */
    private fun allowGeolocationPermissionForSession(origin: String?) {
        val hostName = SiteHost.fromUrl(origin) ?: return
        sessionSitePermissionStore.allow(hostName, SitePermission.LOCATION)
    }

    /**
     * 函数 `denyPermissionPrompt`：拒绝网页定位权限请求并回复 WebView。
     *
     * @param origin 参数类型为 `String?`，表示网页请求定位权限的来源地址，会原样回传给 WebView。
     * @param callback 参数类型为 `GeolocationPermissions.Callback`，表示等待定位权限结果的 WebView 回调。
     */
    private fun denyPermissionPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback
    ) {
        callback.invoke(origin, false, false)
    }

    /**
     * 函数 `androidLocationPermissions`：返回网页定位需要申请的 Android 运行时权限。
     *
     * @return 返回精确定位和大致定位权限数组，调用方会传给 ActivityResultLauncher。
     */
    private fun androidLocationPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    /**
     * 定位权限请求的临时数据。
     *
     * @param origin 参数类型为 `String?`，表示网页请求定位权限的来源地址。
     * @param callback 参数类型为 `GeolocationPermissions.Callback`，表示 WebView 等待定位权限结果的回调。
     */
    private data class GeolocationPermissionPrompt(
        val origin: String?,
        val callback: GeolocationPermissions.Callback
    )
}
