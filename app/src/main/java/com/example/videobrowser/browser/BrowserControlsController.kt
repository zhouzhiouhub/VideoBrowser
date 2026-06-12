package com.example.videobrowser.browser

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
        backButton.setOnClickListener {
            browserManager().goBack()
            updateNavigationButtons()
        }
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

    fun setProgress(progress: Int) {
        pageProgress.progress = progress.coerceIn(0, 100)
    }

    fun updatePageProgressVisibility(isPageLoading: Boolean, forceHidden: Boolean = false) {
        this.isPageLoading = isPageLoading
        updateRefreshButton()
        pageProgress.visibility = when {
            forceHidden || isVideoFullscreenUiActive() || areHidden -> View.GONE
            isPageLoading && pageProgress.progress in 1..99 && !isHomePageVisible() -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }

    fun updateNavigationButtons() {
        val canGoBack = browserManager().canGoBack()
        val visibility = BottomBarButtonVisibility.forPageState(isHomePageVisible())
        backButton.isEnabled = canGoBack
        backButton.alpha = if (canGoBack) 1f else 0.38f
        backButton.visibility = if (visibility.showBack) View.VISIBLE else View.GONE
        pageToolsButton.visibility = if (visibility.showPageTools) View.VISIBLE else View.GONE
        refreshButton.visibility = if (visibility.showRefresh) View.VISIBLE else View.GONE
        wenxinButton.visibility = if (visibility.showWenxin) View.VISIBLE else View.GONE
        profileButton.visibility = if (visibility.showProfile) View.VISIBLE else View.GONE
        bookmarkButton.visibility = View.GONE
        applyBottomBarButtonArrangement(BottomBarButtonArrangement.forVisibility(visibility))
        updateBookmarkButton()
    }

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

        when (arrangement) {
            BottomBarButtonArrangement.HomeSplit -> {
                constraints.connectHorizontalChain(
                    startId = ConstraintSet.PARENT_ID,
                    startSide = ConstraintSet.START,
                    endId = R.id.bottomBarCenterGuide,
                    endSide = ConstraintSet.START,
                    chainIds = intArrayOf(
                        R.id.backButton,
                        R.id.pageToolsButton,
                        R.id.refreshButton,
                        R.id.wenxinButton
                    ),
                    chainStyle = ConstraintSet.CHAIN_PACKED
                )
                constraints.connect(
                    R.id.profileButton,
                    ConstraintSet.START,
                    R.id.bottomBarCenterGuide,
                    ConstraintSet.END
                )
                constraints.connect(
                    R.id.profileButton,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END
                )
            }

            BottomBarButtonArrangement.BrowsingEvenlySpaced -> {
                constraints.connectHorizontalChain(
                    startId = ConstraintSet.PARENT_ID,
                    startSide = ConstraintSet.START,
                    endId = ConstraintSet.PARENT_ID,
                    endSide = ConstraintSet.END,
                    chainIds = intArrayOf(
                        R.id.backButton,
                        R.id.pageToolsButton,
                        R.id.refreshButton,
                        R.id.wenxinButton,
                        R.id.profileButton
                    ),
                    chainStyle = ConstraintSet.CHAIN_SPREAD
                )
            }
        }

        constraints.applyTo(bottomBar)
    }

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
