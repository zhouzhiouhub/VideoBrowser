package com.example.videobrowser.browser.search

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
import com.example.videobrowser.utils.setBoundedSelectableItemBackground

internal class AddressSuggestionRowFactory(
    private val activity: AppCompatActivity,
    private val addressInput: EditText,
    private val dp: (Int) -> Int,
    private val onSuggestionSelected: (AddressSuggestion) -> Unit
) {
    fun create(suggestion: AddressSuggestion): View {
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
                onSuggestionSelected(suggestion)
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

}
