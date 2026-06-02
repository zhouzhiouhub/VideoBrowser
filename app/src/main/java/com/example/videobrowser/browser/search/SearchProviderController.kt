package com.example.videobrowser.browser.search

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.utils.UrlUtils

class SearchProviderController(
    private val activity: AppCompatActivity,
    private val providerScroll: HorizontalScrollView,
    private val providerList: LinearLayout,
    private val addressInput: EditText,
    private val settingsManager: SettingsManager,
    private val dp: (Int) -> Int,
    private val isHomePageVisible: () -> Boolean,
    private val openProviderHome: () -> Unit
) {
    private data class SearchProviderViews(
        val item: LinearLayout,
        val badge: TextView,
        val label: TextView
    )

    private val providers = SearchProviders.defaults
    private val providerViews = mutableMapOf<String, SearchProviderViews>()

    lateinit var selectedProvider: SearchProvider
        private set

    fun setup() {
        selectedProvider = loadSavedSearchProvider()
        if (!settingsManager.hasHomeUrl()) {
            settingsManager.setHomeUrl(selectedProvider.homeUrl)
        }
        providerViews.clear()
        providerList.removeAllViews()

        providers.forEach { provider ->
            val item = createProviderItem(provider)
            val badge = createProviderBadge(provider)
            val label = createProviderLabel(provider)
            item.addView(badge, LinearLayout.LayoutParams(dp(48), dp(48)))
            item.addView(label)

            providerViews[provider.id] = SearchProviderViews(item, badge, label)
            providerList.addView(
                item,
                LinearLayout.LayoutParams(dp(78), ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }

        updateSelection()
    }

    fun syncVisibility(
        areBrowserControlsHidden: Boolean,
        isVideoFullscreenUiActive: Boolean,
        isHomePageVisible: Boolean
    ) {
        providerScroll.visibility =
            if (!areBrowserControlsHidden && !isVideoFullscreenUiActive && isHomePageVisible) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    fun addressBarDisplayText(url: String): String {
        if (isProviderHomeUrl(url)) {
            return ""
        }

        providers.forEach { provider ->
            UrlUtils.searchQueryFromUrl(url, provider.searchUrlPrefix)?.let { return it }
        }
        return UrlUtils.displayUrl(url)
    }

    fun isProviderHomeUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) {
            return false
        }

        val currentUri = Uri.parse(url)
        return providers.any { provider ->
            val homeUri = Uri.parse(provider.homeUrl)
            currentUri.scheme.equals(homeUri.scheme, ignoreCase = true) &&
                currentUri.host.equals(homeUri.host, ignoreCase = true) &&
                normalizedPath(currentUri) == normalizedPath(homeUri)
        }
    }

    private fun createProviderItem(provider: SearchProvider): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            contentDescription = activity.getString(
                R.string.action_select_search_provider,
                provider.name
            )
            setPadding(dp(4), 0, dp(4), 0)
            setSelectableItemBackground()
            setOnClickListener { selectProvider(provider) }
        }
    }

    private fun createProviderBadge(provider: SearchProvider): TextView {
        return TextView(activity).apply {
            gravity = Gravity.CENTER
            includeFontPadding = false
            text = provider.badge
            setTypeface(typeface, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (provider.badge.length > 1) 12f else 16f)
        }
    }

    private fun createProviderLabel(provider: SearchProvider): TextView {
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
            text = provider.name
            textSize = 12f
        }
    }

    private fun selectProvider(provider: SearchProvider) {
        val shouldOpenProviderHome = isHomePageVisible()
        selectedProvider = provider
        settingsManager.setSearchEngineId(provider.id)
        settingsManager.setHomeUrl(provider.homeUrl)
        updateSelection()
        if (shouldOpenProviderHome) {
            openProviderHome()
        }
    }

    private fun loadSavedSearchProvider(): SearchProvider {
        val savedProviderId = settingsManager.searchEngineId()
        return providers.firstOrNull { it.id == savedProviderId } ?: providers.first()
    }

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
        addressInput.hint = activity.getString(
            R.string.hint_search_with_provider,
            selectedProvider.name
        )
    }

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

    private fun View.setSelectableItemBackground() {
        val outValue = TypedValue()
        activity.theme.resolveAttribute(
            android.R.attr.selectableItemBackgroundBorderless,
            outValue,
            true
        )
        setBackgroundResource(outValue.resourceId)
    }

    private fun normalizedPath(uri: Uri): String {
        return uri.path.orEmpty().trim('/')
    }
}
