package com.example.videobrowser.browser.search

/**
 * 初学者阅读提示：
 * 这个文件属于“搜索入口与地址建议模块”。
 * 文件名 SearchProviderController 可以拆开理解为“Search Provider Controller”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：维护默认搜索引擎状态、地址栏搜索源 badge 和搜索结果 URL 的展示文本。
 * 阅读顺序：先看 setup 和 selectDefaultSearchProvider，再看 updateAddressProviderBadge。
 */
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.utils.BrowserDrawableFactory
import com.example.videobrowser.utils.UrlUtils

/**
 * 搜索提供方状态控制器。
 *
 * 它负责维护持久化的搜索引擎选择、同步地址栏搜索源 badge，并把搜索 URL 转回地址栏展示文本。
 * App 首页不再渲染第三方搜索引擎入口、自定义快捷入口或最近访问入口。
 */
class SearchProviderController(
    private val activity: AppCompatActivity,
    private val providerScroll: HorizontalScrollView,
    private val providerList: LinearLayout,
    private val addressInput: EditText,
    private val addressProviderBadge: TextView,
    private val dp: (Int) -> Int,
    private val providers: () -> List<SearchProvider> = { SearchProviders.defaults },
    private val defaultProviderId: () -> String = { SearchProviders.DEFAULT_PROVIDER_ID },
    private val saveDefaultProviderId: (String) -> Unit = {},
    private val builtInSearchResultPagePolicy: BuiltInSearchResultPagePolicy =
        BuiltInSearchResultPagePolicy(providers)
) {
    lateinit var selectedProvider: SearchProvider
        private set

    /**
     * 函数 `setup`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun setup() {
        selectedProvider = loadDefaultSearchProvider()
        providerList.removeAllViews()
        providerScroll.visibility = View.GONE
        updateSelection()
    }

    /**
     * 函数 `syncVisibility`：根据最新状态刷新 `sync Visibility` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param areBrowserControlsHidden 参数类型为 `Boolean`，表示函数执行 `areBrowserControlsHidden` 相关逻辑时需要读取或处理的输入。
     * @param isVideoFullscreenUiActive 参数类型为 `Boolean`，表示函数执行 `isVideoFullscreenUiActive` 相关逻辑时需要读取或处理的输入。
     * @param isHomePageVisible 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun syncVisibility(
        areBrowserControlsHidden: Boolean,
        isVideoFullscreenUiActive: Boolean,
        isHomePageVisible: Boolean
    ) {
        providerScroll.visibility = View.GONE
    }

    /**
     * 函数 `addressBarDisplayText`：封装 `address Bar Display Text` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun addressBarDisplayText(url: String): String {
        return searchQueryFromUrl(url) ?: UrlUtils.displayUrl(url)
    }

    fun searchQueryFromUrl(url: String): String? {
        return builtInSearchResultPagePolicy.searchQueryFromUrl(url)
    }

    fun availableProviders(): List<SearchProvider> {
        return providers()
    }

    /**
     * 函数 `selectDefaultSearchProvider`：封装 `select Default Search Provider` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param providerId 参数类型为 `String`，表示函数执行 `providerId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun selectDefaultSearchProvider(providerId: String): Boolean {
        val provider = availableProviders().firstOrNull { it.id == providerId } ?: return false
        saveDefaultProviderId(provider.id)
        selectedProvider = provider
        updateSelection()
        return true
    }

    /**
     * 函数 `loadDefaultSearchProvider`：启动或加载 `load Default Search Provider` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun loadDefaultSearchProvider(): SearchProvider {
        val availableProviders = availableProviders()
        val configuredProviderId = defaultProviderId().trim()
        return availableProviders.firstOrNull { it.id == configuredProviderId }
            ?: availableProviders.firstOrNull { it.id == SearchProviders.DEFAULT_PROVIDER_ID }
            ?: availableProviders.first()
    }

    /**
     * 函数 `updateSelection`：根据最新状态刷新 `update Selection` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun updateSelection() {
        addressInput.hint = activity.getString(R.string.hint_address_bar)
        updateAddressProviderBadge()
    }

    /**
     * 函数 `updateAddressProviderBadge`：根据最新状态刷新 `update Address Provider Badge` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun updateAddressProviderBadge() {
        addressProviderBadge.text = selectedProvider.badge
        addressProviderBadge.setTextColor(Color.WHITE)
        addressProviderBadge.setTypeface(addressProviderBadge.typeface, Typeface.BOLD)
        addressProviderBadge.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            if (selectedProvider.badge.length > 1) 9f else 12f
        )
        addressProviderBadge.background = createProviderBadgeBackground(
            selectedProvider,
            selected = true
        )
    }

    /**
     * 函数 `createProviderBadgeBackground`：创建 `create Provider Badge Background` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param provider 参数类型为 `SearchProvider`，表示函数执行 `provider` 相关逻辑时需要读取或处理的输入。
     * @param selected 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun createProviderBadgeBackground(
        provider: SearchProvider,
        selected: Boolean
    ): GradientDrawable {
        return if (selected) {
            BrowserDrawableFactory.circleBackground(
                color = provider.accentColor,
                strokeWidth = dp(2),
                strokeColor = ContextCompat.getColor(
                    activity,
                    R.color.browser_provider_selected_stroke
                )
            )
        } else {
            BrowserDrawableFactory.circleBackground(
                ContextCompat.getColor(activity, R.color.browser_provider_circle)
            )
        }
    }

}
