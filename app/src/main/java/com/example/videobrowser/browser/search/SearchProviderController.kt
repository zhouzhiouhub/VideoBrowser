package com.example.videobrowser.browser.search

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.InputType
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videobrowser.R
import com.example.videobrowser.settings.CustomShortcut
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.utils.UrlUtils

class SearchProviderController(
    private val activity: AppCompatActivity,
    private val providerScroll: HorizontalScrollView,
    private val providerList: LinearLayout,
    private val addressInput: EditText,
    private val addressProviderBadge: TextView,
    private val settingsManager: SettingsManager,
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

    lateinit var selectedProvider: SearchProvider
        private set

    fun setup() {
        selectedProvider = loadSavedSearchProvider()
        if (!isPrivateBrowsingEnabled() && !settingsManager.hasHomeUrl()) {
            settingsManager.setHomeUrl(selectedProvider.homeUrl)
        }
        providerViews.clear()
        providerList.removeAllViews()

        providers.forEach { provider ->
            addProviderItem(provider)
        }
        settingsManager.customShortcuts().forEach { shortcut ->
            addCustomShortcutItem(shortcut)
        }
        addAddShortcutItem()

        updateSelection()
    }

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

    fun selectDefaultSearchProvider(providerId: String): Boolean {
        val provider = providers.firstOrNull { it.id == providerId } ?: return false
        selectedProvider = provider
        settingsManager.setSearchEngineId(provider.id)
        updateSelection()
        return true
    }

    private fun addProviderItem(provider: SearchProvider) {
        val item = createProviderItem(provider)
        val badge = createProviderBadge(provider)
        val label = createProviderLabel(provider)
        item.addView(badge, LinearLayout.LayoutParams(dp(48), dp(48)))
        item.addView(label)

        providerViews[provider.id] = SearchProviderViews(item, badge, label)
        providerList.addView(item, providerItemLayoutParams())
    }

    private fun addCustomShortcutItem(shortcut: CustomShortcut) {
        val item = createCustomShortcutItem(shortcut)
        item.addView(
            createCustomShortcutBadge(shortcut),
            LinearLayout.LayoutParams(dp(48), dp(48))
        )
        item.addView(createCustomShortcutLabel(shortcut.name))
        providerList.addView(item, providerItemLayoutParams())
    }

    private fun addAddShortcutItem() {
        val item = createAddShortcutItem()
        item.addView(createAddShortcutBadge(), LinearLayout.LayoutParams(dp(48), dp(48)))
        item.addView(createCustomShortcutLabel(activity.getString(R.string.action_add)))
        providerList.addView(item, providerItemLayoutParams())
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
            setBoundedSelectableItemBackground()
            setOnClickListener { selectProvider(provider) }
        }
    }

    private fun createCustomShortcutItem(shortcut: CustomShortcut): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            contentDescription = activity.getString(
                R.string.action_open_custom_shortcut,
                shortcut.name
            )
            setPadding(dp(4), 0, dp(4), 0)
            setBoundedSelectableItemBackground()
            setOnClickListener { openCustomShortcut(shortcut.url) }
            setOnLongClickListener {
                showCustomShortcutActionsDialog(shortcut)
                true
            }
        }
    }

    private fun createAddShortcutItem(): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            contentDescription = activity.getString(R.string.action_add_custom_shortcut)
            setPadding(dp(4), 0, dp(4), 0)
            setBoundedSelectableItemBackground()
            setOnClickListener { showAddShortcutDialog() }
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

    private fun createCustomShortcutBadge(shortcut: CustomShortcut): TextView {
        val badgeText = shortcutBadgeText(shortcut.name)
        return TextView(activity).apply {
            gravity = Gravity.CENTER
            includeFontPadding = false
            text = badgeText
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(activity, R.color.browser_primary))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (badgeText.length > 1) 12f else 16f)
            background = createCircleBackground(
                ContextCompat.getColor(activity, R.color.browser_provider_circle)
            )
        }
    }

    private fun createAddShortcutBadge(): ImageView {
        return ImageView(activity).apply {
            setImageResource(R.drawable.ic_add_24)
            setColorFilter(ContextCompat.getColor(activity, R.color.browser_primary))
            background = createCircleBackground(
                ContextCompat.getColor(activity, R.color.browser_provider_circle)
            )
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
    }

    private fun createProviderLabel(provider: SearchProvider): TextView {
        return createCustomShortcutLabel(provider.name)
    }

    private fun createCustomShortcutLabel(labelText: String): TextView {
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
        addressInput.hint = activity.getString(R.string.hint_address_bar)
        updateAddressProviderBadge()
    }

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

    private fun createCircleBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }
    }

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

    private fun providerItemLayoutParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(dp(78), ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun shortcutBadgeText(name: String): String {
        return name.trim().take(2).ifBlank { "+" }
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

    private fun normalizedPath(uri: Uri): String {
        return uri.path.orEmpty().trim('/')
    }
}
