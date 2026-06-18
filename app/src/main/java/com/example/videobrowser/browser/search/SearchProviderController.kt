package com.example.videobrowser.browser.search

/**
 * 初学者阅读提示：
 * 这个文件属于“搜索入口与地址建议模块”。
 * 文件名 SearchProviderController 可以拆开理解为“Search Provider Controller”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：把地址栏输入、默认搜索引擎、远程搜索建议、收藏和历史候选项整理成用户可以点击的建议列表。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.settings.CustomShortcut
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.utils.UrlUtils

/**
 * 首页搜索入口控制器。
 *
 * 它负责渲染搜索引擎按钮、自定义快捷入口和最近访问入口，并把用户选择保存到 SettingsManager。
 * MainActivity 只关心“当前选中的搜索引擎是谁”，具体 UI 细节都留在这里。
 */
class SearchProviderController(
    private val activity: AppCompatActivity,
    private val providerScroll: HorizontalScrollView,
    private val providerList: LinearLayout,
    private val addressInput: EditText,
    private val addressProviderBadge: TextView,
    private val settingsManager: SettingsManager,
    private val savedPageRepository: SavedPageRepository,
    private val dp: (Int) -> Int,
    private val isHomePageVisible: () -> Boolean,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val openProviderHome: () -> Unit,
    private val openCustomShortcut: (String) -> Unit
) {
    private data class SearchProviderViews(
        val item: LinearLayout,
        val badge: TextView,
        val label: TextView
    )

    private val providers = SearchProviders.defaults
    private val providerViews = mutableMapOf<String, SearchProviderViews>()
    private val itemFactory = SearchProviderItemFactory(
        activity = activity,
        dp = dp,
        onProviderSelected = ::selectProvider,
        onCustomShortcutOpen = openCustomShortcut,
        onCustomShortcutLongClick = { shortcut -> showCustomShortcutActionsDialog(shortcut) },
        onRecentHistoryOpen = openCustomShortcut,
        onRecentHistoryLongClick = { quickLink -> showRemoveRecentHistoryDialog(quickLink) },
        onAddShortcut = ::showAddShortcutDialog
    )

    lateinit var selectedProvider: SearchProvider
        private set

    /**
     * 函数 `setup`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun setup() {
        // 每次重新进入主页都会重建列表，确保自定义快捷入口、历史入口和默认搜索引擎都是最新状态。
        selectedProvider = loadSavedSearchProvider()
        if (!isPrivateBrowsingEnabled() && !settingsManager.hasHomeUrl()) {
            settingsManager.setHomeUrl(selectedProvider.homeUrl)
        }
        providerViews.clear()
        providerList.removeAllViews()

        providers.forEach { provider ->
            addProviderItem(provider)
        }
        val customShortcuts = settingsManager.customShortcuts()
        customShortcuts.forEach { shortcut ->
            addCustomShortcutItem(shortcut)
        }
        if (!isPrivateBrowsingEnabled()) {
            HomeQuickLinkBuilder.fromHistory(
                history = savedPageRepository.history(),
                excludedUrls = homeQuickLinkExcludedUrls(customShortcuts)
            ).forEach { quickLink ->
                addRecentHistoryItem(quickLink)
            }
        }
        addAddShortcutItem()

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
        providerScroll.visibility = if (
            !areBrowserControlsHidden &&
            !isVideoFullscreenUiActive &&
            !isPrivateBrowsingEnabled() &&
            isHomePageVisible
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    /**
     * 函数 `addressBarDisplayText`：封装 `address Bar Display Text` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun addressBarDisplayText(url: String): String {
        if (isProviderHomeUrl(url)) {
            return ""
        }

        providers.forEach { provider ->
            UrlUtils.searchQueryFromUrl(url, provider.searchUrlPrefix)?.let { return it }
        }
        return UrlUtils.displayUrl(url)
    }

    /**
     * 函数 `isProviderHomeUrl`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isProviderHomeUrl(url: String?): Boolean {
        return SearchProviderHomeMatcher.isProviderHomeUrl(url, providers)
    }

    /**
     * 函数 `selectDefaultSearchProvider`：封装 `select Default Search Provider` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param providerId 参数类型为 `String`，表示函数执行 `providerId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun selectDefaultSearchProvider(providerId: String): Boolean {
        val provider = providers.firstOrNull { it.id == providerId } ?: return false
        selectedProvider = provider
        settingsManager.setSearchEngineId(provider.id)
        updateSelection()
        return true
    }

    /**
     * 函数 `addProviderItem`：封装 `add Provider Item` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param provider 参数类型为 `SearchProvider`，表示函数执行 `provider` 相关逻辑时需要读取或处理的输入。
     */
    private fun addProviderItem(provider: SearchProvider) {
        val item = itemFactory.createProviderItem(provider)
        val badge = itemFactory.createProviderBadge(provider)
        val label = itemFactory.createProviderLabel(provider)
        item.addView(badge, LinearLayout.LayoutParams(dp(48), dp(48)))
        item.addView(label)

        providerViews[provider.id] = SearchProviderViews(item, badge, label)
        providerList.addView(item, itemFactory.providerItemLayoutParams())
    }

    /**
     * 函数 `addCustomShortcutItem`：封装 `add Custom Shortcut Item` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param shortcut 参数类型为 `CustomShortcut`，表示函数执行 `shortcut` 相关逻辑时需要读取或处理的输入。
     */
    private fun addCustomShortcutItem(shortcut: CustomShortcut) {
        val item = itemFactory.createCustomShortcutItem(shortcut)
        item.addView(
            itemFactory.createCustomShortcutBadge(shortcut),
            LinearLayout.LayoutParams(dp(48), dp(48))
        )
        item.addView(itemFactory.createCustomShortcutLabel(shortcut.name))
        providerList.addView(item, itemFactory.providerItemLayoutParams())
    }

    /**
     * 函数 `addRecentHistoryItem`：封装 `add Recent History Item` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param quickLink 参数类型为 `HomeQuickLink`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    private fun addRecentHistoryItem(quickLink: HomeQuickLink) {
        val item = createRecentHistoryItem(quickLink)
        item.addView(itemFactory.createRecentHistoryBadge(), LinearLayout.LayoutParams(dp(48), dp(48)))
        item.addView(itemFactory.createCustomShortcutLabel(quickLink.title))
        providerList.addView(item, itemFactory.providerItemLayoutParams())
    }

    /**
     * 函数 `addAddShortcutItem`：封装 `add Add Shortcut Item` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun addAddShortcutItem() {
        val item = itemFactory.createAddShortcutItem()
        item.addView(itemFactory.createAddShortcutBadge(), LinearLayout.LayoutParams(dp(48), dp(48)))
        item.addView(itemFactory.createCustomShortcutLabel(activity.getString(R.string.action_add)))
        providerList.addView(item, itemFactory.providerItemLayoutParams())
    }

    private fun createRecentHistoryItem(quickLink: HomeQuickLink): LinearLayout {
        return itemFactory.createRecentHistoryItem(quickLink)
    }

    /**
     * 函数 `selectProvider`：封装 `select Provider` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param provider 参数类型为 `SearchProvider`，表示函数执行 `provider` 相关逻辑时需要读取或处理的输入。
     */
    private fun selectProvider(provider: SearchProvider) {
        val shouldOpenProviderHome = isHomePageVisible()
        selectedProvider = provider
        if (!isPrivateBrowsingEnabled()) {
            settingsManager.setSearchEngineId(provider.id)
            settingsManager.setHomeUrl(provider.homeUrl)
        }
        updateSelection()
        if (shouldOpenProviderHome) {
            openProviderHome()
        }
    }

    /**
     * 函数 `loadSavedSearchProvider`：启动或加载 `load Saved Search Provider` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun loadSavedSearchProvider(): SearchProvider {
        val savedProviderId = settingsManager.searchEngineId()
        return providers.firstOrNull { it.id == savedProviderId } ?: providers.first()
    }

    /**
     * 函数 `updateSelection`：根据最新状态刷新 `update Selection` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun updateSelection() {
        providers.forEach { provider ->
            val views = providerViews[provider.id] ?: return@forEach
            val selected = provider.id == selectedProvider.id
            views.item.isSelected = selected
            views.badge.background = createProviderBadgeBackground(provider, selected)
            views.badge.setTextColor(
                if (selected) {
                    Color.WHITE
                } else {
                    ContextCompat.getColor(activity, R.color.browser_icon)
                }
            )
            views.label.setTextColor(
                ContextCompat.getColor(
                    activity,
                    if (selected) R.color.browser_text else R.color.browser_text_hint
                )
            )
            views.label.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
        }
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
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            if (selected) {
                setColor(provider.accentColor)
                setStroke(
                    dp(2),
                    ContextCompat.getColor(activity, R.color.browser_provider_selected_stroke)
                )
            } else {
                setColor(ContextCompat.getColor(activity, R.color.browser_provider_circle))
            }
        }
    }

    /**
     * 函数 `showAddShortcutDialog`：控制 `show Add Shortcut Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showAddShortcutDialog() {
        showCustomShortcutEditorDialog(
            titleResId = R.string.title_add_custom_shortcut,
            initialName = "",
            initialUrl = "",
            positiveButtonResId = R.string.action_add,
            successToastResId = R.string.toast_custom_shortcut_added
        ) { name, url ->
            settingsManager.addCustomShortcut(name, url)
        }
    }

    /**
     * 函数 `showCustomShortcutActionsDialog`：控制 `show Custom Shortcut Actions Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param shortcut 参数类型为 `CustomShortcut`，表示函数执行 `shortcut` 相关逻辑时需要读取或处理的输入。
     */
    private fun showCustomShortcutActionsDialog(shortcut: CustomShortcut) {
        val actions = listOf(
            activity.getString(R.string.action_edit) to { showEditCustomShortcutDialog(shortcut) },
            activity.getString(R.string.action_remove) to { showRemoveCustomShortcutDialog(shortcut) }
        )
        AlertDialog.Builder(activity)
            .setTitle(shortcut.name)
            .setItems(actions.map { action -> action.first }.toTypedArray()) { _, index ->
                actions.getOrNull(index)?.second?.invoke()
            }
            .show()
    }

    /**
     * 函数 `showEditCustomShortcutDialog`：控制 `show Edit Custom Shortcut Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param shortcut 参数类型为 `CustomShortcut`，表示函数执行 `shortcut` 相关逻辑时需要读取或处理的输入。
     */
    private fun showEditCustomShortcutDialog(shortcut: CustomShortcut) {
        showCustomShortcutEditorDialog(
            titleResId = R.string.title_edit_custom_shortcut,
            initialName = shortcut.name,
            initialUrl = shortcut.url,
            positiveButtonResId = R.string.action_save,
            successToastResId = R.string.toast_custom_shortcut_updated
        ) { name, url ->
            settingsManager.updateCustomShortcut(shortcut, name, url)
        }
    }

    /**
     * 函数 `showCustomShortcutEditorDialog`：控制 `show Custom Shortcut Editor Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param titleResId 参数类型为 `Int`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param initialName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param initialUrl 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param positiveButtonResId 参数类型为 `Int`，表示函数执行 `positiveButtonResId` 相关逻辑时需要读取或处理的输入。
     * @param successToastResId 参数类型为 `Int`，表示函数执行 `successToastResId` 相关逻辑时需要读取或处理的输入。
     * @param saveShortcut 参数类型为 `(String, String) -> Boolean`，表示函数执行 `saveShortcut` 相关逻辑时需要读取或处理的输入。
     */
    private fun showCustomShortcutEditorDialog(
        titleResId: Int,
        initialName: String,
        initialUrl: String,
        positiveButtonResId: Int,
        successToastResId: Int,
        saveShortcut: (String, String) -> Boolean
    ) {
        val nameInput = EditText(activity).apply {
            hint = activity.getString(R.string.hint_custom_shortcut_name)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            isSingleLine = true
            setText(initialName)
        }
        val urlInput = EditText(activity).apply {
            hint = activity.getString(R.string.hint_custom_shortcut_url)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            isSingleLine = true
            setText(initialUrl)
        }
        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(nameInput)
            addView(urlInput)
        }

        val dialog = AlertDialog.Builder(activity)
            .setTitle(titleResId)
            .setView(content)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(positiveButtonResId, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val saved = saveShortcut(
                    nameInput.text?.toString().orEmpty(),
                    urlInput.text?.toString().orEmpty()
                )
                if (saved) {
                    setup()
                    Toast.makeText(
                        activity,
                        successToastResId,
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(
                        activity,
                        R.string.toast_custom_shortcut_invalid,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        dialog.show()
    }

    /**
     * 函数 `showRemoveCustomShortcutDialog`：控制 `show Remove Custom Shortcut Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param shortcut 参数类型为 `CustomShortcut`，表示函数执行 `shortcut` 相关逻辑时需要读取或处理的输入。
     */
    private fun showRemoveCustomShortcutDialog(shortcut: CustomShortcut) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_remove_custom_shortcut)
            .setMessage(
                activity.getString(
                    R.string.dialog_remove_custom_shortcut_message,
                    shortcut.name
                )
            )
            .setPositiveButton(R.string.action_remove) { _, _ ->
                if (settingsManager.removeCustomShortcut(shortcut)) {
                    setup()
                    Toast.makeText(
                        activity,
                        R.string.toast_custom_shortcut_removed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `showRemoveRecentHistoryDialog`：控制 `show Remove Recent History Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param quickLink 参数类型为 `HomeQuickLink`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     */
    private fun showRemoveRecentHistoryDialog(quickLink: HomeQuickLink) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_remove_recent_site)
            .setMessage(
                activity.getString(
                    R.string.dialog_remove_recent_site_message,
                    quickLink.title
                )
            )
            .setPositiveButton(R.string.action_remove) { _, _ ->
                val removed = savedPageRepository.remove(
                    SavedPageRepository.SavedPageCollection.HISTORY,
                    quickLink.url
                )
                if (removed) {
                    setup()
                    Toast.makeText(
                        activity,
                        R.string.toast_recent_site_removed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * 函数 `homeQuickLinkExcludedUrls`：封装 `home Quick Link Excluded Urls` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param customShortcuts 参数类型为 `List<CustomShortcut>`，表示函数执行 `customShortcuts` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun homeQuickLinkExcludedUrls(customShortcuts: List<CustomShortcut>): List<String> {
        return customShortcuts.map { shortcut -> shortcut.url } +
            providers.map { provider -> provider.homeUrl } +
            settingsManager.homeUrlOr(selectedProvider.homeUrl)
    }

}
