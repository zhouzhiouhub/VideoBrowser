package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器站点安全 UI 模块”。
 * 文件名 SiteSecurityController 可以拆开理解为“Site Security Controller”，表示它专门负责地址栏安全图标和安全详情弹窗。
 * 主要职责：根据当前网页 URL 展示 HTTPS/HTTP 安全状态，并把证书说明、混合内容策略和站点设置入口组织成用户可读信息。
 * 阅读顺序：先看构造参数知道它依赖 MainActivity 提供哪些数据，再看 setup/updateStatus/showInfoDialog 三个入口。
 */
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.videobrowser.R
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.utils.AppDialog
import com.example.videobrowser.utils.UrlUtils

/**
 * 地址栏站点安全控制器。
 *
 * MainActivity 只负责在页面 URL 变化时调用本类；本类负责把安全状态映射为图标、颜色、无障碍描述和详情弹窗。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来读取字符串资源、创建对话框和取得颜色资源上下文。
 * @param siteSecurityIcon 参数类型为 `ImageView`，表示地址栏里的安全状态图标，本类会更新它的图片、颜色、可见性和点击行为。
 * @param settingsManager 参数类型为 `SettingsManager`，表示浏览器设置入口，用来读取混合内容拦截策略并展示到安全详情里。
 * @param currentPageUrl 参数类型为 `() -> String?`，表示当前会话记录的页面地址，安全详情会优先使用它作为用户正在访问的 URL。
 * @param currentWebViewUrl 参数类型为 `() -> String?`，表示当前 WebView 实时地址，当会话地址为空时作为备用来源。
 * @param isPrivateBrowsingEnabled 参数类型为 `() -> Boolean`，表示当前是否处于无痕模式，用来决定是否显示“站点设置”入口。
 * @param currentSiteHost 参数类型为 `() -> String?`，表示当前站点 host，用来判断当前页面是否能进入站点级设置。
 * @param showCurrentSiteSettingsPage 参数类型为 `() -> Unit`，表示打开当前站点设置页的回调，用户点击详情弹窗中性按钮时调用。
 */
class SiteSecurityController(
    private val activity: AppCompatActivity,
    private val siteSecurityIcon: ImageView,
    private val settingsManager: SettingsManager,
    private val currentPageUrl: () -> String?,
    private val currentWebViewUrl: () -> String?,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val currentSiteHost: () -> String?,
    private val showCurrentSiteSettingsPage: () -> Unit
) {
    /**
     * 函数 `setup`：连接地址栏安全图标的点击入口。
     *
     * 初学者阅读提示：这个函数只做一次性 UI 绑定；后续 URL 变化由 updateStatus 负责刷新图标状态。
     */
    fun setup() {
        siteSecurityIcon.setOnClickListener {
            showInfoDialog()
        }
    }

    /**
     * 函数 `updateStatus`：根据最新页面地址刷新地址栏安全图标。
     *
     * 初学者阅读提示：先把 URL 转换成 SiteSecurityStatus，再根据状态选择锁图标、警告图标或隐藏图标。
     *
     * @param url 参数类型为 `String?`，表示需要判断安全状态的页面地址；为空、非 HTTP 或非 HTTPS 时会隐藏安全图标。
     */
    fun updateStatus(url: String?) {
        when (SiteSecurityStatus.fromUrl(url)) {
            SiteSecurityStatus.SECURE -> showStatus(
                iconResId = R.drawable.ic_lock_24,
                colorResId = R.color.site_security_secure,
                descriptionResId = R.string.site_security_secure
            )

            SiteSecurityStatus.NOT_SECURE -> showStatus(
                iconResId = R.drawable.ic_warning_24,
                colorResId = R.color.site_security_insecure,
                descriptionResId = R.string.site_security_not_secure
            )

            SiteSecurityStatus.UNKNOWN -> hideStatus()
        }
    }

    /**
     * 函数 `showInfoDialog`：展示当前页面的站点安全详情。
     *
     * 初学者阅读提示：这个函数会合并会话 URL 和 WebView URL，过滤掉无法识别安全状态的页面，再拼出用户能读懂的说明。
     */
    fun showInfoDialog() {
        val pageUrl = listOf(
            currentPageUrl(),
            currentWebViewUrl()
        ).firstOrNull { url -> !url.isNullOrBlank() } ?: return
        val status = SiteSecurityStatus.fromUrl(pageUrl)
        if (status == SiteSecurityStatus.UNKNOWN) {
            return
        }

        val message = securityMessage(pageUrl, status)
        val builder = AppDialog.builder(activity)
            .setTitle(R.string.title_site_security_info)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
        if (!isPrivateBrowsingEnabled() && currentSiteHost() != null) {
            builder.setNeutralButton(R.string.action_site_settings) { _, _ ->
                showCurrentSiteSettingsPage()
            }
        }
        builder.show()
    }

    /**
     * 函数 `showStatus`：把安全状态展示到地址栏图标。
     *
     * 初学者阅读提示：这个函数集中设置图标资源、颜色、可见性、可点击状态和无障碍描述，避免调用方遗漏某一项 UI 状态。
     *
     * @param iconResId 参数类型为 `Int`，表示要显示的 drawable 资源，例如锁图标或警告图标。
     * @param colorResId 参数类型为 `Int`，表示要应用到图标上的颜色资源，用来区分安全和不安全状态。
     * @param descriptionResId 参数类型为 `Int`，表示安全状态文案资源，会用于 contentDescription 和 tooltip。
     */
    private fun showStatus(
        iconResId: Int,
        colorResId: Int,
        descriptionResId: Int
    ) {
        val description = activity.getString(descriptionResId)
        val actionDescription = activity.getString(R.string.site_security_icon_description, description)
        siteSecurityIcon.visibility = View.VISIBLE
        siteSecurityIcon.isEnabled = true
        siteSecurityIcon.setImageResource(iconResId)
        siteSecurityIcon.setColorFilter(ContextCompat.getColor(activity, colorResId))
        siteSecurityIcon.contentDescription = actionDescription
        ViewCompat.setTooltipText(siteSecurityIcon, actionDescription)
    }

    /**
     * 函数 `hideStatus`：隐藏地址栏安全状态图标并清理辅助说明。
     *
     * 初学者阅读提示：UNKNOWN 状态不能给用户明确安全结论，所以这里同时隐藏图标、清空描述并禁用点击。
     */
    private fun hideStatus() {
        siteSecurityIcon.visibility = View.GONE
        siteSecurityIcon.contentDescription = null
        ViewCompat.setTooltipText(siteSecurityIcon, null)
        siteSecurityIcon.isEnabled = false
    }

    /**
     * 函数 `securityMessage`：组合站点安全详情弹窗的正文。
     *
     * 初学者阅读提示：正文由状态标题、host、展示用 URL、协议、证书说明和混合内容说明组成，每一段之间用空行分隔。
     *
     * @param pageUrl 参数类型为 `String`，表示正在展示安全详情的页面地址，用来计算 host 和展示用 URL。
     * @param status 参数类型为 `SiteSecurityStatus`，表示页面安全状态，用来选择标题、协议和说明文案。
     * @return 返回拼接后的详情正文；调用方会把它传给 AlertDialog 展示。
     */
    private fun securityMessage(pageUrl: String, status: SiteSecurityStatus): String {
        val statusTitleResId = when (status) {
            SiteSecurityStatus.SECURE -> R.string.site_security_secure
            SiteSecurityStatus.NOT_SECURE -> R.string.site_security_not_secure
            SiteSecurityStatus.UNKNOWN -> R.string.title_site_security_info
        }
        val messageResId = when (status) {
            SiteSecurityStatus.SECURE -> R.string.site_security_secure_message
            SiteSecurityStatus.NOT_SECURE -> R.string.site_security_not_secure_message
            SiteSecurityStatus.UNKNOWN -> R.string.site_security_unknown_message
        }
        val displayUrl = UrlUtils.displayUrl(pageUrl)
        val host = SiteHost.fromUrl(pageUrl)
            ?: displayUrl
        return listOf(
            activity.getString(statusTitleResId),
            activity.getString(R.string.site_security_host, host),
            activity.getString(R.string.site_security_url, displayUrl),
            activity.getString(R.string.site_security_protocol, status.protocolDisplayName()),
            activity.getString(messageResId),
            certificateSummary(status),
            mixedContentSummary(status)
        ).joinToString(separator = "\n\n")
    }

    /**
     * 函数 `certificateSummary`：返回当前安全状态对应的证书说明。
     *
     * 初学者阅读提示：HTTPS 页面显示证书已验证，HTTP 页面说明没有使用证书，未知状态只作为兜底文案。
     *
     * @param status 参数类型为 `SiteSecurityStatus`，表示页面安全状态，用来决定证书说明文案。
     * @return 返回本地化后的证书说明，供安全详情弹窗正文使用。
     */
    private fun certificateSummary(status: SiteSecurityStatus): String {
        return when (status) {
            SiteSecurityStatus.SECURE -> activity.getString(R.string.site_security_certificate_validated)
            SiteSecurityStatus.NOT_SECURE -> activity.getString(R.string.site_security_certificate_not_used)
            SiteSecurityStatus.UNKNOWN -> activity.getString(R.string.site_security_unknown_message)
        }
    }

    /**
     * 函数 `mixedContentSummary`：返回当前安全状态对应的混合内容策略说明。
     *
     * 初学者阅读提示：只有 HTTPS 页面需要说明混合内容策略；HTTP 页面本身不适用混合内容降级判断。
     *
     * @param status 参数类型为 `SiteSecurityStatus`，表示页面安全状态，用来决定是否读取混合内容拦截设置。
     * @return 返回本地化后的混合内容说明，供安全详情弹窗正文使用。
     */
    private fun mixedContentSummary(status: SiteSecurityStatus): String {
        return when (status) {
            SiteSecurityStatus.SECURE -> if (settingsManager.isMixedContentBlocked()) {
                activity.getString(R.string.site_security_mixed_content_blocked)
            } else {
                activity.getString(R.string.site_security_mixed_content_compatibility)
            }

            SiteSecurityStatus.NOT_SECURE -> activity.getString(R.string.site_security_mixed_content_not_applicable)
            SiteSecurityStatus.UNKNOWN -> activity.getString(R.string.site_security_unknown_message)
        }
    }
}
