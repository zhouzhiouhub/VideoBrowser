package com.example.videobrowser.browser.search

/**
 * 初学者阅读提示：
 * 这个文件属于“搜索入口与地址建议模块”。
 * 文件名 AddressSuggestionController 可以拆开理解为“Address Suggestion Controller”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：把地址栏输入、默认搜索引擎、远程搜索建议、收藏和历史候选项整理成用户可以点击的建议列表。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPageRepository

/**
 * 地址栏建议面板控制器。
 *
 * 用户在地址栏输入时，这个类负责先显示本地建议，再延迟请求远程建议。
 * requestSequence 用来区分新旧请求，避免慢返回的旧网络结果覆盖用户最新输入。
 */
class AddressSuggestionController(
    private val activity: AppCompatActivity,
    private val panel: LinearLayout,
    private val addressInput: EditText,
    private val savedPageRepository: SavedPageRepository,
    private val suggestionClient: SearchSuggestionClient,
    private val selectedProvider: () -> SearchProvider,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val areBrowserControlsHidden: () -> Boolean,
    private val isVideoFullscreenUiActive: () -> Boolean,
    private val openUrl: (String) -> Unit,
    private val searchKeyword: (String) -> Unit,
    private val dp: (Int) -> Int
) {
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var requestSequence = 0
    private var suppressTextChanges = false
    private var disposed = false

    /**
     * 函数 `setup`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun setup() {
        addressInput.addTextChangedListener(
            object : TextWatcher {
                /**
                 * 函数 `beforeTextChanged`：封装 `before Text Changed` 这一段业务步骤，让调用方不用关心内部实现细节。
                 *
                 * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
                 * @param s 参数类型为 `CharSequence?`，表示函数执行 `s` 相关逻辑时需要读取或处理的输入。
                 * @param start 参数类型为 `Int`，表示函数执行 `start` 相关逻辑时需要读取或处理的输入。
                 * @param count 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
                 * @param after 参数类型为 `Int`，表示函数执行 `after` 相关逻辑时需要读取或处理的输入。
                 * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
                 */
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                /**
                 * 函数 `onTextChanged`：处理 `on Text Changed` 对应的事件或请求，集中完成校验、状态更新和回调通知。
                 *
                 * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
                 * @param s 参数类型为 `CharSequence?`，表示函数执行 `s` 相关逻辑时需要读取或处理的输入。
                 * @param start 参数类型为 `Int`，表示函数执行 `start` 相关逻辑时需要读取或处理的输入。
                 * @param before 参数类型为 `Int`，表示函数执行 `before` 相关逻辑时需要读取或处理的输入。
                 * @param count 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
                 * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
                 */
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                /**
                 * 函数 `afterTextChanged`：封装 `after Text Changed` 这一段业务步骤，让调用方不用关心内部实现细节。
                 *
                 * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
                 * @param s 参数类型为 `Editable?`，表示函数执行 `s` 相关逻辑时需要读取或处理的输入。
                 */
                override fun afterTextChanged(s: Editable?) {
                    if (!suppressTextChanges) {
                        handleInputChanged()
                    }
                }
            }
        )
    }

    /**
     * 函数 `handleAddressFocusChanged`：处理 `handle Address Focus Changed` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param hasFocus 参数类型为 `Boolean`，表示函数执行 `hasFocus` 相关逻辑时需要读取或处理的输入。
     */
    fun handleAddressFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            handleInputChanged()
        } else {
            hide()
        }
    }

    /**
     * 函数 `syncVisibility`：根据最新状态刷新 `sync Visibility` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun syncVisibility() {
        if (!canShowSuggestions()) {
            hide()
        }
    }

    /**
     * 函数 `hide`：控制 `hide` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun hide() {
        requestSequence += 1
        handler.removeCallbacksAndMessages(null)
        panel.visibility = View.GONE
        panel.removeAllViews()
    }

    /**
     * 函数 `dispose`：封装 `dispose` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun dispose() {
        disposed = true
        hide()
        suggestionClient.dispose()
    }

    /**
     * 函数 `runWithSuggestionsSuppressed`：封装 `run With Suggestions Suppressed` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param action 参数类型为 `() -> Unit`，表示函数执行 `action` 相关逻辑时需要读取或处理的输入。
     */
    fun runWithSuggestionsSuppressed(action: () -> Unit) {
        suppressTextChanges = true
        hide()
        try {
            action()
        } finally {
            suppressTextChanges = false
        }
    }

    /**
     * 函数 `handleInputChanged`：处理 `handle Input Changed` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun handleInputChanged() {
        if (disposed) {
            return
        }
        // 每次输入变化都递增序号，后续远程回调只有序号仍匹配时才允许更新 UI。
        requestSequence += 1
        handler.removeCallbacksAndMessages(null)
        val query = currentQuery()
        if (!canShowSuggestions(query)) {
            hide()
            return
        }

        renderSuggestions(query, remoteKeywords = emptyList())
        if (isPrivateBrowsingEnabled()) {
            // 无痕模式不读取历史/收藏，也不请求远程建议，减少可被记录的输入痕迹。
            return
        }

        val sequence = requestSequence
        handler.postDelayed(
            {
                suggestionClient.fetch(selectedProvider(), query) { remoteKeywords ->
                    activity.runOnUiThread {
                        if (
                            !disposed &&
                            sequence == requestSequence &&
                            currentQuery() == query &&
                            canShowSuggestions(query)
                        ) {
                            renderSuggestions(query, remoteKeywords)
                        }
                    }
                }
            },
            REMOTE_DEBOUNCE_MS
        )
    }

    /**
     * 函数 `renderSuggestions`：封装 `render Suggestions` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param query 参数类型为 `String`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     * @param remoteKeywords 参数类型为 `List<String>`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    private fun renderSuggestions(query: String, remoteKeywords: List<String>) {
        val includePrivateSources = !isPrivateBrowsingEnabled()
        val suggestions = AddressSuggestionRanker.build(
            input = query,
            history = if (includePrivateSources) savedPageRepository.history() else emptyList(),
            bookmarks = if (includePrivateSources) savedPageRepository.bookmarks() else emptyList(),
            remoteKeywords = remoteKeywords,
            includePrivateSources = includePrivateSources
        )
        if (suggestions.isEmpty()) {
            hide()
            return
        }

        panel.removeAllViews()
        suggestions.forEach { suggestion ->
            panel.addView(createSuggestionRow(suggestion))
        }
        panel.visibility = View.VISIBLE
    }

    /**
     * 函数 `createSuggestionRow`：创建 `create Suggestion Row` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param suggestion 参数类型为 `AddressSuggestion`，表示函数执行 `suggestion` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun createSuggestionRow(suggestion: AddressSuggestion): View {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            setPadding(dp(18), 0, dp(18), 0)
            setBoundedSelectableItemBackground()
            addView(createIcon(suggestion), LinearLayout.LayoutParams(dp(28), dp(28)))
            addView(
                createTextContainer(suggestion),
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(12)
                }
            )
            minimumHeight = when (suggestion) {
                is AddressSuggestion.Bookmark,
                is AddressSuggestion.History -> dp(58)
                is AddressSuggestion.Remote,
                is AddressSuggestion.Fallback -> dp(48)
            }
            contentDescription = contentDescriptionFor(suggestion)
            setOnClickListener {
                selectSuggestion(suggestion)
            }
        }
    }

    /**
     * 函数 `createIcon`：创建 `create Icon` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param suggestion 参数类型为 `AddressSuggestion`，表示函数执行 `suggestion` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun createIcon(suggestion: AddressSuggestion): ImageView {
        return ImageView(activity).apply {
            setImageResource(
                when (suggestion) {
                    is AddressSuggestion.Bookmark -> R.drawable.ic_star_24
                    is AddressSuggestion.History -> R.drawable.ic_history_24
                    is AddressSuggestion.Remote,
                    is AddressSuggestion.Fallback -> R.drawable.ic_search_24
                }
            )
            setColorFilter(ContextCompat.getColor(activity, R.color.browser_primary))
            setPadding(dp(2), dp(2), dp(2), dp(2))
        }
    }

    /**
     * 函数 `createTextContainer`：创建 `create Text Container` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param suggestion 参数类型为 `AddressSuggestion`，表示函数执行 `suggestion` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun createTextContainer(suggestion: AddressSuggestion): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            when (suggestion) {
                is AddressSuggestion.Bookmark -> {
                    addView(createPrimaryText(suggestion.title))
                    addView(createSecondaryText(suggestion.displayUrl))
                }
                is AddressSuggestion.History -> {
                    addView(createPrimaryText(suggestion.title))
                    addView(createSecondaryText(suggestion.displayUrl))
                }
                is AddressSuggestion.Remote -> addView(createPrimaryText(suggestion.keyword))
                is AddressSuggestion.Fallback -> {
                    addView(
                        createPrimaryText(
                            activity.getString(R.string.address_suggestion_search, suggestion.keyword)
                        )
                    )
                }
            }
        }
    }

    /**
     * 函数 `createPrimaryText`：创建 `create Primary Text` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param textValue 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun createPrimaryText(textValue: String): TextView {
        return TextView(activity).apply {
            text = textValue
            includeFontPadding = false
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setTextColor(addressInput.currentTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        }
    }

    /**
     * 函数 `createSecondaryText`：创建 `create Secondary Text` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param textValue 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun createSecondaryText(textValue: String): TextView {
        return TextView(activity).apply {
            text = textValue
            includeFontPadding = false
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setTextColor(addressInput.currentHintTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(5)
            }
        }
    }

    /**
     * 函数 `selectSuggestion`：封装 `select Suggestion` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param suggestion 参数类型为 `AddressSuggestion`，表示函数执行 `suggestion` 相关逻辑时需要读取或处理的输入。
     */
    private fun selectSuggestion(suggestion: AddressSuggestion) {
        runWithSuggestionsSuppressed {
            when (suggestion) {
                is AddressSuggestion.Bookmark -> openUrl(suggestion.url)
                is AddressSuggestion.History -> openUrl(suggestion.url)
                is AddressSuggestion.Remote -> searchKeyword(suggestion.keyword)
                is AddressSuggestion.Fallback -> searchKeyword(suggestion.keyword)
            }
        }
    }

    /**
     * 函数 `contentDescriptionFor`：封装 `content Description For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param suggestion 参数类型为 `AddressSuggestion`，表示函数执行 `suggestion` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun contentDescriptionFor(suggestion: AddressSuggestion): String {
        return when (suggestion) {
            is AddressSuggestion.Bookmark -> {
                activity.getString(R.string.address_suggestion_bookmark, suggestion.title)
            }
            is AddressSuggestion.History -> {
                activity.getString(R.string.address_suggestion_history, suggestion.title)
            }
            is AddressSuggestion.Remote -> {
                activity.getString(R.string.address_suggestion_keyword, suggestion.keyword)
            }
            is AddressSuggestion.Fallback -> {
                activity.getString(R.string.address_suggestion_search, suggestion.keyword)
            }
        }
    }

    /**
     * 函数 `canShowSuggestions`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param query 参数类型为 `String`，表示函数执行 `query` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun canShowSuggestions(query: String = currentQuery()): Boolean {
        return addressInput.hasFocus() &&
            query.isNotBlank() &&
            !areBrowserControlsHidden() &&
            !isVideoFullscreenUiActive()
    }

    /**
     * 函数 `currentQuery`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun currentQuery(): String {
        return addressInput.text?.toString()?.trim().orEmpty()
    }

    /**
     * 函数 `setBoundedSelectableItemBackground`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun View.setBoundedSelectableItemBackground() {
        val outValue = TypedValue()
        activity.theme.resolveAttribute(
            android.R.attr.selectableItemBackground,
            outValue,
            true
        )
        setBackgroundResource(outValue.resourceId)
    }

    private companion object {
        private const val REMOTE_DEBOUNCE_MS = 300L
    }
}
