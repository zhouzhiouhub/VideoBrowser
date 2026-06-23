package com.example.videobrowser.browser.search

import android.graphics.Typeface
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.settings.CustomShortcut
import com.example.videobrowser.utils.BrowserDrawableFactory
import com.example.videobrowser.utils.setBoundedSelectableItemBackground

internal class SearchProviderItemFactory(
    private val activity: AppCompatActivity,
    private val dp: (Int) -> Int,
    private val onProviderSelected: (SearchProvider) -> Unit,
    private val onCustomShortcutOpen: (String) -> Unit,
    private val onCustomShortcutLongClick: (CustomShortcut) -> Unit,
    private val onRecentHistoryOpen: (String) -> Unit,
    private val onRecentHistoryLongClick: (HomeQuickLink) -> Unit,
    private val onAddShortcut: () -> Unit
) {
    fun createProviderItem(provider: SearchProvider): LinearLayout {
        return createSelectableHomeItem(
            contentDescription = activity.getString(
                R.string.action_select_search_provider,
                provider.name
            ),
            onClick = { onProviderSelected(provider) }
        )
    }

    fun createCustomShortcutItem(shortcut: CustomShortcut): LinearLayout {
        return createSelectableHomeItem(
            contentDescription = activity.getString(
                R.string.action_open_custom_shortcut,
                shortcut.name
            ),
            onClick = { onCustomShortcutOpen(shortcut.url) },
            onLongClick = { onCustomShortcutLongClick(shortcut) }
        )
    }

    fun createRecentHistoryItem(quickLink: HomeQuickLink): LinearLayout {
        return createSelectableHomeItem(
            contentDescription = activity.getString(
                R.string.action_open_recent_site,
                quickLink.title
            ),
            onClick = { onRecentHistoryOpen(quickLink.url) },
            onLongClick = { onRecentHistoryLongClick(quickLink) }
        )
    }

    fun createAddShortcutItem(): LinearLayout {
        return createSelectableHomeItem(
            contentDescription = activity.getString(R.string.action_add_custom_shortcut),
            onClick = { onAddShortcut() }
        )
    }

    private fun createSelectableHomeItem(
        contentDescription: String,
        onClick: () -> Unit,
        onLongClick: (() -> Unit)? = null
    ): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            this.contentDescription = contentDescription
            setPadding(dp(4), 0, dp(4), 0)
            setBoundedSelectableItemBackground()
            setOnClickListener { onClick() }
            onLongClick?.let { handler ->
                setOnLongClickListener {
                    handler()
                    true
                }
            }
        }
    }

    fun createRecentHistoryBadge(): ImageView {
        return createIconBadge(R.drawable.ic_history_24)
    }

    fun createProviderBadge(provider: SearchProvider): TextView {
        return TextView(activity).apply {
            gravity = Gravity.CENTER
            includeFontPadding = false
            text = provider.badge
            setTypeface(typeface, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (provider.badge.length > 1) 12f else 16f)
        }
    }

    fun createCustomShortcutBadge(shortcut: CustomShortcut): TextView {
        val badgeText = shortcutBadgeText(shortcut.name)
        return TextView(activity).apply {
            gravity = Gravity.CENTER
            includeFontPadding = false
            text = badgeText
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(activity, R.color.browser_primary))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (badgeText.length > 1) 12f else 16f)
            background = providerCircleBackground()
        }
    }

    fun createAddShortcutBadge(): ImageView {
        return createIconBadge(R.drawable.ic_add_24)
    }

    private fun createIconBadge(iconResId: Int): ImageView {
        return ImageView(activity).apply {
            setImageResource(iconResId)
            setColorFilter(ContextCompat.getColor(activity, R.color.browser_primary))
            background = providerCircleBackground()
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
    }

    private fun providerCircleBackground() = BrowserDrawableFactory.circleBackground(
        ContextCompat.getColor(activity, R.color.browser_provider_circle)
    )

    fun createProviderLabel(provider: SearchProvider): TextView {
        return createCustomShortcutLabel(provider.name)
    }

    fun createCustomShortcutLabel(labelText: String): TextView {
        return TextView(activity).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.CENTER
            includeFontPadding = false
            maxLines = 1
            text = labelText
            setTextColor(ContextCompat.getColor(activity, R.color.browser_text))
            textSize = 12f
        }
    }

    fun providerItemLayoutParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(dp(78), ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun shortcutBadgeText(name: String): String {
        return name.trim().take(2).ifBlank { "+" }
    }

}
