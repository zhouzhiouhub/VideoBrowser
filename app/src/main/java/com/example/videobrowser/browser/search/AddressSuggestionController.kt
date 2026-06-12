package com.example.videobrowser.browser.search

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

    fun setup() {
        addressInput.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                override fun afterTextChanged(s: Editable?) {
                    if (!suppressTextChanges) {
                        handleInputChanged()
                    }
                }
            }
        )
    }

    fun handleAddressFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            handleInputChanged()
        } else {
            hide()
        }
    }

    fun syncVisibility() {
        if (!canShowSuggestions()) {
            hide()
        }
    }

    fun hide() {
        requestSequence += 1
        handler.removeCallbacksAndMessages(null)
        panel.visibility = View.GONE
        panel.removeAllViews()
    }

    fun runWithSuggestionsSuppressed(action: () -> Unit) {
        suppressTextChanges = true
        hide()
        try {
            action()
        } finally {
            suppressTextChanges = false
        }
    }

    private fun handleInputChanged() {
        requestSequence += 1
        handler.removeCallbacksAndMessages(null)
        val query = currentQuery()
        if (!canShowSuggestions(query)) {
            hide()
            return
        }

        renderSuggestions(query, remoteKeywords = emptyList())
        if (isPrivateBrowsingEnabled()) {
            return
        }

        val sequence = requestSequence
        handler.postDelayed(
            {
                suggestionClient.fetch(selectedProvider(), query) { remoteKeywords ->
                    activity.runOnUiThread {
                        if (
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

    private fun canShowSuggestions(query: String = currentQuery()): Boolean {
        return addressInput.hasFocus() &&
            query.isNotBlank() &&
            !areBrowserControlsHidden() &&
            !isVideoFullscreenUiActive()
    }

    private fun currentQuery(): String {
        return addressInput.text?.toString()?.trim().orEmpty()
    }

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
