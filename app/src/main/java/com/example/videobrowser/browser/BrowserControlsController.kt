package com.example.videobrowser.browser

import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.KeyEvent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPageRepository

class BrowserControlsController(
    private val activity: AppCompatActivity,
    private val browserManager: () -> BrowserManager,
    private val topBar: View,
    private val bottomBar: View,
    private val addressInput: EditText,
    private val pageProgress: ProgressBar,
    private val pageToolsButton: ImageButton,
    private val backButton: ImageButton,
    private val refreshButton: ImageButton,
    private val homeButton: ImageButton,
    private val bookmarkButton: ImageButton,
    private val loadButton: ImageButton,
    private val savedPageRepository: SavedPageRepository,
    private val currentActionableUrl: () -> String?,
    private val isHomePageVisible: () -> Boolean,
    private val isVideoFullscreenUiActive: () -> Boolean,
    private val onLoadAddress: () -> Unit,
    private val onOpenHomePage: () -> Unit,
    private val onShowFunctionCenter: () -> Unit,
    private val onToggleBookmark: () -> Unit,
    private val onShowControlsRequested: () -> Unit,
    private val onVisibilityChanged: () -> Unit
) {
    var areHidden = false
        private set

    fun setup() {
        ViewCompat.setTooltipText(pageToolsButton, activity.getString(R.string.title_page_tools))
        ViewCompat.setTooltipText(loadButton, activity.getString(R.string.action_load_url))
        ViewCompat.setTooltipText(backButton, activity.getString(R.string.action_back))
        ViewCompat.setTooltipText(refreshButton, activity.getString(R.string.action_refresh))
        ViewCompat.setTooltipText(homeButton, activity.getString(R.string.action_home))
        ViewCompat.setTooltipText(bookmarkButton, activity.getString(R.string.action_add_bookmark))

        addressInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                onShowControlsRequested()
                addressInput.selectAll()
            }
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
        refreshButton.setOnClickListener { browserManager().reload() }
        homeButton.setOnClickListener { onOpenHomePage() }
        bookmarkButton.setOnClickListener { onToggleBookmark() }
        pageToolsButton.setOnClickListener { onShowFunctionCenter() }

        updateNavigationButtons()
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
        pageProgress.visibility = when {
            forceHidden || isVideoFullscreenUiActive() || areHidden -> View.GONE
            isPageLoading && pageProgress.progress in 1..99 && !isHomePageVisible() -> View.VISIBLE
            else -> View.INVISIBLE
        }
    }

    fun updateNavigationButtons() {
        val canGoBack = browserManager().canGoBack()
        val homeVisible = isHomePageVisible()
        backButton.isEnabled = canGoBack
        backButton.visibility = if (canGoBack) View.VISIBLE else View.GONE
        homeButton.visibility = if (homeVisible) View.GONE else View.VISIBLE
        bookmarkButton.visibility = if (homeVisible) View.GONE else View.VISIBLE
        updateBookmarkButton()
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
}
