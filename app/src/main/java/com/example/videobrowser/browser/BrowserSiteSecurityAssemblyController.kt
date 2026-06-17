package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“站点安全装配模块”。
 * 文件名 BrowserSiteSecurityAssemblyController 可以拆开理解为“Browser Site Security Assembly Controller”，
 * 表示它只负责创建地址栏站点安全图标和详情弹窗控制器。
 * 阅读顺序：先看 create() 的参数传递，理解站点安全状态需要当前页面 URL、WebView URL、无痕状态和站点设置入口。
 */
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.settings.SettingsManager

/**
 * 站点安全装配控制器。
 *
 * SiteSecurityController 会在 URL、主题或站点设置入口变化时刷新地址栏图标；本类把它的依赖集中起来。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示创建站点安全详情弹窗和读取字符串资源的宿主 Activity。
 * @param siteSecurityIcon 参数类型为 `ImageView`，表示地址栏中展示锁/警告状态的图标控件。
 * @param settingsManager 参数类型为 `SettingsManager`，表示读取混合内容策略等站点安全设置的数据源。
 * @param browserSessionStateController 参数类型为 `BrowserSessionStateController`，表示读取当前页面 URL 的控制器。
 * @param browserStandardWebViewHostController 参数类型为 `BrowserStandardWebViewHostController`，表示读取当前 WebView 实际 URL 的宿主控制器。
 * @param browserFeatureStateController 参数类型为 `BrowserFeatureStateController`，表示读取无痕浏览状态的控制器。
 * @param browserUrlStateController 参数类型为 `BrowserUrlStateController`，表示读取当前站点 host 的控制器。
 * @param showCurrentSiteSettingsPage 参数类型为 `() -> Unit`，表示打开当前站点设置页的回调。
 */
class BrowserSiteSecurityAssemblyController(
    private val activity: AppCompatActivity,
    private val siteSecurityIcon: ImageView,
    private val settingsManager: SettingsManager,
    private val browserSessionStateController: BrowserSessionStateController,
    private val browserStandardWebViewHostController: BrowserStandardWebViewHostController,
    private val browserFeatureStateController: BrowserFeatureStateController,
    private val browserUrlStateController: BrowserUrlStateController,
    private val showCurrentSiteSettingsPage: () -> Unit
) {
    /**
     * 创建站点安全控制器。
     *
     * @return 返回 `SiteSecurityController`，调用方保存后交给地址栏状态、主题和浏览器外壳控制器刷新。
     */
    fun create(): SiteSecurityController {
        return SiteSecurityController(
            activity = activity,
            siteSecurityIcon = siteSecurityIcon,
            settingsManager = settingsManager,
            currentPageUrl = {
                browserSessionStateController.currentSessionController().currentPageUrl
            },
            currentWebViewUrl = {
                browserStandardWebViewHostController.currentBrowserManager().currentUrl()
            },
            isPrivateBrowsingEnabled = browserFeatureStateController::isPrivateBrowsingEnabled,
            currentSiteHost = browserUrlStateController::currentSiteHost,
            showCurrentSiteSettingsPage = showCurrentSiteSettingsPage
        )
    }
}
