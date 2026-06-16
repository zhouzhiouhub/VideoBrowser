package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserControlsController 可以拆开理解为“Browser Controls Controller”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.KeyEvent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPageRepository

class BrowserControlsController(
    private val activity: AppCompatActivity,
    private val browserManager: () -> BrowserManager,
    private val topBar: View,
    private val bottomBar: ConstraintLayout,
    private val addressInput: EditText,
    private val pageProgress: ProgressBar,
    private val pageToolsButton: ImageButton,
    private val wenxinButton: ImageButton,
    private val profileButton: ImageButton,
    private val backButton: ImageButton,
    private val refreshButton: ImageButton,
    private val bookmarkButton: ImageButton,
    private val loadButton: ImageButton,
    private val savedPageRepository: SavedPageRepository,
    private val currentActionableUrl: () -> String?,
    private val isHomePageVisible: () -> Boolean,
    private val isVideoFullscreenUiActive: () -> Boolean,
    private val onLoadAddress: () -> Unit,
    private val onBack: () -> Unit,
    private val onOpenWenxin: () -> Unit,
    private val onShowFunctionCenter: () -> Unit,
    private val onShowProfilePage: () -> Unit,
    private val onToggleBookmark: () -> Unit,
    private val onShowControlsRequested: () -> Unit,
    private val onAddressFocusChanged: (Boolean) -> Unit,
    private val onVisibilityChanged: () -> Unit
) {
    var areHidden = false
        private set
    private var isPageLoading = false

    /**
     * 函数 `setup`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun setup() {
        ViewCompat.setTooltipText(pageToolsButton, activity.getString(R.string.title_page_tools))
        ViewCompat.setTooltipText(wenxinButton, activity.getString(R.string.action_wenxin))
        ViewCompat.setTooltipText(profileButton, activity.getString(R.string.action_profile))
        ViewCompat.setTooltipText(loadButton, activity.getString(R.string.action_load_url))
        ViewCompat.setTooltipText(backButton, activity.getString(R.string.action_back))
        ViewCompat.setTooltipText(refreshButton, activity.getString(R.string.action_refresh))
        ViewCompat.setTooltipText(bookmarkButton, activity.getString(R.string.action_add_bookmark))

        addressInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                onShowControlsRequested()
                addressInput.selectAll()
            }
            onAddressFocusChanged(hasFocus)
        }

        addressInput.setOnEditorActionListener { _, actionId, event ->
            val isEnterUp =
                event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP
            if (actionId == EditorInfo.IME_ACTION_SEARCH || isEnterUp) {
                onLoadAddress()
                true
            } else {
                false
            }
        }

        loadButton.setOnClickListener { onLoadAddress() }
        backButton.setOnClickListener { onBack() }
        refreshButton.setOnClickListener {
            if (isPageLoading) {
                browserManager().stopLoading()
            } else {
                browserManager().reload()
            }
        }
        wenxinButton.setOnClickListener { onOpenWenxin() }
        profileButton.setOnClickListener { onShowProfilePage() }
        bookmarkButton.setOnClickListener { onToggleBookmark() }
        pageToolsButton.setOnClickListener { onShowFunctionCenter() }

        updateNavigationButtons()
        updateRefreshButton()
    }

    /**
     * 函数 `setHidden`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param hidden 参数类型为 `Boolean`，表示函数执行 `hidden` 相关逻辑时需要读取或处理的输入。
     */
    fun setHidden(hidden: Boolean) {
        if (areHidden == hidden) {
            onVisibilityChanged()
            return
        }

        areHidden = hidden
        topBar.visibility = if (hidden) View.GONE else View.VISIBLE
        bottomBar.visibility = if (hidden) View.GONE else View.VISIBLE
        onVisibilityChanged()
    }

    /**
     * 函数 `setProgress`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param progress 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    fun setProgress(progress: Int) {
        pageProgress.progress = progress.coerceIn(0, 100)
    }

    /**
     * 函数 `updatePageProgressVisibility`：根据最新状态刷新 `update Page Progress Visibility` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param isPageLoading 参数类型为 `Boolean`，表示函数执行 `isPageLoading` 相关逻辑时需要读取或处理的输入。
     * @param forceHidden 参数类型为 `Boolean`，表示函数执行 `forceHidden` 相关逻辑时需要读取或处理的输入。
     */
    fun updatePageProgressVisibility(isPageLoading: Boolean, forceHidden: Boolean = false) {
        this.isPageLoading = isPageLoading
        updateRefreshButton()
        pageProgress.visibility = when {
            forceHidden || isVideoFullscreenUiActive() || areHidden -> View.GONE
            isPageLoading && pageProgress.progress in 1..99 && !isHomePageVisible() -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }

    /**
     * 函数 `updateNavigationButtons`：根据最新状态刷新 `update Navigation Buttons` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun updateNavigationButtons() {
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible())
        backButton.isEnabled = visibility.showBack
        backButton.alpha = if (visibility.showBack) 1f else 0.38f
        backButton.visibility = if (visibility.showBack) View.VISIBLE else View.GONE
        pageToolsButton.visibility = if (visibility.showPageTools) View.VISIBLE else View.GONE
        refreshButton.visibility = if (visibility.showRefresh) View.VISIBLE else View.GONE
        wenxinButton.visibility = if (visibility.showWenxin) View.VISIBLE else View.GONE
        profileButton.visibility = if (visibility.showProfile) View.VISIBLE else View.GONE
        bookmarkButton.visibility = View.GONE
        applyBottomBarButtonArrangement(BottomBarButtonArrangement.forVisibility(visibility))
        updateBookmarkButton()
    }

    /**
     * 函数 `applyBottomBarButtonArrangement`：根据最新状态刷新 `apply Bottom Bar Button Arrangement` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param arrangement 参数类型为 `BottomBarButtonArrangement`，表示函数执行 `arrangement` 相关逻辑时需要读取或处理的输入。
     */
    private fun applyBottomBarButtonArrangement(arrangement: BottomBarButtonArrangement) {
        val constraints = ConstraintSet().apply { clone(bottomBar) }
        val actionIds = intArrayOf(
            R.id.backButton,
            R.id.pageToolsButton,
            R.id.refreshButton,
            R.id.wenxinButton,
            R.id.profileButton
        )

        actionIds.forEach { id ->
            constraints.clear(id, ConstraintSet.START)
            constraints.clear(id, ConstraintSet.END)
            constraints.clear(id, ConstraintSet.TOP)
            constraints.clear(id, ConstraintSet.BOTTOM)
            constraints.connect(id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            constraints.connect(id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        }

        val visibleActionIds = actionIds
            .filter { id -> bottomBar.findViewById<View>(id).visibility == View.VISIBLE }
            .toIntArray()

        when (arrangement) {
            BottomBarButtonArrangement.VisibleActionsEvenlySpaced -> {
                if (visibleActionIds.isEmpty()) {
                    constraints.applyTo(bottomBar)
                    return
                }
                constraints.connectHorizontalChain(
                    startId = ConstraintSet.PARENT_ID,
                    startSide = ConstraintSet.START,
                    endId = ConstraintSet.PARENT_ID,
                    endSide = ConstraintSet.END,
                    chainIds = visibleActionIds,
                    chainStyle = ConstraintSet.CHAIN_SPREAD
                )
            }
        }

        constraints.applyTo(bottomBar)
    }

    /**
     * 函数 `connectHorizontalChain`：封装 `connect Horizontal Chain` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param startId 参数类型为 `Int`，表示函数执行 `startId` 相关逻辑时需要读取或处理的输入。
     * @param startSide 参数类型为 `Int`，表示函数执行 `startSide` 相关逻辑时需要读取或处理的输入。
     * @param endId 参数类型为 `Int`，表示函数执行 `endId` 相关逻辑时需要读取或处理的输入。
     * @param endSide 参数类型为 `Int`，表示函数执行 `endSide` 相关逻辑时需要读取或处理的输入。
     * @param chainIds 参数类型为 `IntArray`，表示函数执行 `chainIds` 相关逻辑时需要读取或处理的输入。
     * @param chainStyle 参数类型为 `Int`，表示函数执行 `chainStyle` 相关逻辑时需要读取或处理的输入。
     */
    private fun ConstraintSet.connectHorizontalChain(
        startId: Int,
        startSide: Int,
        endId: Int,
        endSide: Int,
        chainIds: IntArray,
        chainStyle: Int
    ) {
        chainIds.forEachIndexed { index, id ->
            val previousId = chainIds.getOrNull(index - 1)
            val nextId = chainIds.getOrNull(index + 1)
            connect(
                id,
                ConstraintSet.START,
                previousId ?: startId,
                previousId?.let { ConstraintSet.END } ?: startSide
            )
            connect(
                id,
                ConstraintSet.END,
                nextId ?: endId,
                nextId?.let { ConstraintSet.START } ?: endSide
            )
        }
        setHorizontalChainStyle(chainIds.first(), chainStyle)
    }

    /**
     * 函数 `updateBookmarkButton`：根据最新状态刷新 `update Bookmark Button` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun updateBookmarkButton() {
        if (isHomePageVisible()) {
            bookmarkButton.isEnabled = false
            return
        }
        val url = currentActionableUrl()
        val isEnabled = url != null
        val isBookmarked = url?.let(savedPageRepository::isBookmarked) ?: false
        val actionText = activity.getString(
            if (isBookmarked) R.string.action_remove_bookmark else R.string.action_add_bookmark
        )

        bookmarkButton.isEnabled = isEnabled
        bookmarkButton.contentDescription = actionText
        bookmarkButton.setImageResource(
            if (isBookmarked) R.drawable.ic_star_filled_24 else R.drawable.ic_star_24
        )
        bookmarkButton.setColorFilter(
            ContextCompat.getColor(
                activity,
                when {
                    !isEnabled -> R.color.browser_icon_disabled
                    isBookmarked -> R.color.bookmark_active
                    else -> R.color.browser_icon
                }
            )
        )
        ViewCompat.setTooltipText(bookmarkButton, actionText)
    }

    /**
     * 函数 `updateRefreshButton`：根据最新状态刷新 `update Refresh Button` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun updateRefreshButton() {
        val actionText = activity.getString(
            if (isPageLoading) R.string.action_stop_loading else R.string.action_refresh
        )
        refreshButton.contentDescription = actionText
        refreshButton.setImageResource(
            if (isPageLoading) R.drawable.ic_stop_24 else R.drawable.ic_refresh_24
        )
        ViewCompat.setTooltipText(refreshButton, actionText)
    }
}
