package com.example.videobrowser.browser.search

/**
 * 初学者阅读提示：
 * 这个文件属于“搜索与地址栏装配模块”。
 * 文件名 BrowserSearchAssemblyController 可以拆开理解为“Browser Search Assembly Controller”，
 * 表示它只负责创建搜索入口、地址栏状态、历史记录过滤策略和地址建议控制器。
 * 阅读顺序：先看 BrowserSearchComponents 知道返回哪些对象，再看 create() 里各控制器如何共享搜索引擎状态。
 */
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.browser.BrowserAddressBarStateController
import com.example.videobrowser.browser.BrowserHomePageUrlPolicy
import com.example.videobrowser.browser.BrowserPageFeatureVisibilityController
import com.example.videobrowser.browser.BrowserPageFeatureVisibilityPolicy
import com.example.videobrowser.browser.HistoryRecordPolicy
import com.example.videobrowser.browser.SiteSecurityController
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.SavedPageRepository

/**
 * 搜索与地址栏组件集合。
 *
 * @param searchProviderController 参数类型为 `SearchProviderController`，表示管理默认搜索引擎和地址栏搜索源标识的控制器。
 * @param builtInSearchResultPagePolicy 参数类型为 `BuiltInSearchResultPagePolicy`，表示识别 App 内置搜索引擎结果页的策略。
 * @param pageFeatureVisibilityController 参数类型为 `BrowserPageFeatureVisibilityController`，表示页面增强清理完成前的 WebView 可见性控制器。
 * @param browserAddressBarStateController 参数类型为 `BrowserAddressBarStateController`，表示同步地址栏文本和站点安全状态的控制器。
 * @param homePageUrlPolicy 参数类型为 `BrowserHomePageUrlPolicy`，表示识别哪些恢复 URL 应显示为 App 自定义首页的策略。
 * @param historyRecordPolicy 参数类型为 `HistoryRecordPolicy`，表示判断页面 URL 是否应该写入浏览历史的策略。
 * @param addressSuggestionController 参数类型为 `AddressSuggestionController`，表示地址栏输入建议面板控制器。
 */
data class BrowserSearchComponents(
    val searchProviderController: SearchProviderController,
    val builtInSearchResultPagePolicy: BuiltInSearchResultPagePolicy,
    val pageFeatureVisibilityController: BrowserPageFeatureVisibilityController,
    val browserAddressBarStateController: BrowserAddressBarStateController,
    val homePageUrlPolicy: BrowserHomePageUrlPolicy,
    val historyRecordPolicy: HistoryRecordPolicy,
    val addressSuggestionController: AddressSuggestionController
)

/**
 * 浏览器搜索与地址栏装配控制器。
 *
 * MainActivity 需要这些对象供后续导航、启动和生命周期控制器使用；本类集中维护它们之间的共享依赖，
 * 尤其是默认搜索引擎和主页 URL 对历史记录过滤策略的影响。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示创建搜索入口和地址建议 UI 的宿主 Activity。
 * @param providerScroll 参数类型为 `HorizontalScrollView`，表示旧首页搜索入口横向滚动容器；本阶段保持隐藏。
 * @param providerList 参数类型为 `LinearLayout`，表示旧首页搜索入口列表容器；本阶段不再填充入口。
 * @param addressInput 参数类型为 `EditText`，表示浏览器地址栏输入框。
 * @param addressProviderBadge 参数类型为 `TextView`，表示地址栏旁当前搜索引擎标识。
 * @param addressSuggestionPanel 参数类型为 `LinearLayout`，表示地址栏建议项显示容器。
 * @param activeWebView 参数类型为 `() -> View`，表示读取当前 active WebView 的回调，用于页面增强首屏隐藏。
 * @param settingsManager 参数类型为 `SettingsManager`，表示读取默认搜索引擎、主页 URL 和快捷入口的设置管理器。
 * @param savedPageRepository 参数类型为 `SavedPageRepository`，表示搜索入口和地址建议使用的收藏/历史仓库。
 * @param siteSecurityController 参数类型为 `() -> SiteSecurityController?`，表示返回站点安全控制器的函数，尚未初始化时返回 null。
 * @param dp 参数类型为 `(Int) -> Int`，表示把 dp 数值转换为像素的函数。
 * @param isPrivateBrowsingEnabled 参数类型为 `() -> Boolean`，表示读取当前是否处于无痕浏览的函数。
 * @param areBrowserControlsHidden 参数类型为 `() -> Boolean`，表示读取浏览器上下工具栏是否隐藏的函数。
 * @param isVideoFullscreenUiActive 参数类型为 `() -> Boolean`，表示读取视频全屏浮层是否处于激活状态的函数。
 * @param openUrl 参数类型为 `(String) -> Unit`，表示地址建议选择普通 URL 时加载网页的回调。
 * @param searchKeyword 参数类型为 `(String) -> Unit`，表示地址建议选择搜索词时发起搜索的回调。
 */
class BrowserSearchAssemblyController(
    private val activity: AppCompatActivity,
    private val providerScroll: HorizontalScrollView,
    private val providerList: LinearLayout,
    private val addressInput: EditText,
    private val addressProviderBadge: TextView,
    private val addressSuggestionPanel: LinearLayout,
    private val activeWebView: () -> View,
    private val settingsManager: SettingsManager,
    private val savedPageRepository: SavedPageRepository,
    private val siteSecurityController: () -> SiteSecurityController?,
    private val dp: (Int) -> Int,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val areBrowserControlsHidden: () -> Boolean,
    private val isVideoFullscreenUiActive: () -> Boolean,
    private val openUrl: (String) -> Unit,
    private val searchKeyword: (String) -> Unit
) {
    /**
     * 创建搜索与地址栏组件集合。
     *
     * @return 返回 `BrowserSearchComponents`，调用方把其中对象保存到对应字段后继续创建导航和启动控制器。
     */
    fun create(): BrowserSearchComponents {
        val providers = SearchProviders.defaults
        val builtInSearchResultPagePolicy = BuiltInSearchResultPagePolicy(providers)
        val searchProviderController = SearchProviderController(
            activity = activity,
            providerScroll = providerScroll,
            providerList = providerList,
            addressInput = addressInput,
            addressProviderBadge = addressProviderBadge,
            settingsManager = settingsManager,
            dp = dp,
            providers = providers,
            builtInSearchResultPagePolicy = builtInSearchResultPagePolicy
        )
        val pageFeatureVisibilityPolicy = BrowserPageFeatureVisibilityPolicy(
            settingsManager = settingsManager,
            isBuiltInSearchResultPage = builtInSearchResultPagePolicy::isBuiltInSearchResultUrl
        )
        val pageFeatureVisibilityController =
            BrowserPageFeatureVisibilityController(
                setActiveWebViewAlpha = { alpha -> activeWebView().alpha = alpha },
                shouldHideUntilPageFeaturesInjected =
                    pageFeatureVisibilityPolicy::shouldHideUntilPageFeaturesInjected
            )
        val browserAddressBarStateController = BrowserAddressBarStateController(
            addressInput = addressInput,
            searchProviderController = searchProviderController,
            siteSecurityController = siteSecurityController
        )
        val homePageUrlPolicy = BrowserHomePageUrlPolicy(
            homeUrls = {
                SearchProviders.defaults.map { provider -> provider.homeUrl } +
                    settingsManager.homeUrlOr(searchProviderController.selectedProvider.homeUrl)
            }
        )
        val historyRecordPolicy = HistoryRecordPolicy(
            homePageUrlPolicy = homePageUrlPolicy
        )
        val addressSuggestionController = AddressSuggestionController(
            activity = activity,
            panel = addressSuggestionPanel,
            addressInput = addressInput,
            savedPageRepository = savedPageRepository,
            suggestionClient = SearchSuggestionClient(),
            selectedProvider = { searchProviderController.selectedProvider },
            isPrivateBrowsingEnabled = isPrivateBrowsingEnabled,
            areBrowserControlsHidden = areBrowserControlsHidden,
            isVideoFullscreenUiActive = isVideoFullscreenUiActive,
            openUrl = openUrl,
            searchKeyword = searchKeyword,
            dp = dp
        )
        return BrowserSearchComponents(
            searchProviderController = searchProviderController,
            builtInSearchResultPagePolicy = builtInSearchResultPagePolicy,
            pageFeatureVisibilityController = pageFeatureVisibilityController,
            browserAddressBarStateController = browserAddressBarStateController,
            homePageUrlPolicy = homePageUrlPolicy,
            historyRecordPolicy = historyRecordPolicy,
            addressSuggestionController = addressSuggestionController
        )
    }
}
