package com.example.videobrowser.functioncenter

import android.text.InputType
import android.widget.LinearLayout
import com.example.videobrowser.R
import com.example.videobrowser.browser.search.SearchProvider
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.utils.DensityPixelConverter
import com.example.videobrowser.utils.ShortToast
import com.example.videobrowser.utils.TextInputDialogField
import com.example.videobrowser.utils.TwoTextInputDialog

class SearchEngineSettingsPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val availableSearchProviders: () -> List<SearchProvider>,
    private val currentSearchProviderId: () -> String,
    private val selectSearchProvider: (String) -> Boolean,
    private val showProfilePage: () -> Unit
) {
    private val activity = host.activity

    fun show(replaceCurrent: Boolean = false) {
        val providers = availableSearchProviders()
        val selectedProviderId = currentSearchProviderId()
        host.showPage(
            title = activity.getString(R.string.setting_search_engine),
            onBack = showProfilePage,
            replaceCurrent = replaceCurrent
        ) { content ->
            host.contentFactory.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_settings)
            ) { section ->
                providers.forEach { provider ->
                    addProviderRow(section, provider, selectedProviderId)
                }
            }
            addCustomSearchEngineButton(content)
        }
    }

    private fun addProviderRow(
        section: LinearLayout,
        provider: SearchProvider,
        selectedProviderId: String
    ) {
        val selected = provider.id == selectedProviderId
        host.contentFactory.addActionRow(
            parent = section,
            title = provider.name,
            summary = if (selected) {
                activity.getString(
                    R.string.search_engine_selected_summary,
                    provider.searchUrlPrefix
                )
            } else {
                provider.searchUrlPrefix
            }
        ) {
            if (!selected && selectSearchProvider(provider.id)) {
                ShortToast.show(activity, R.string.toast_search_engine_updated)
                show(replaceCurrent = true)
            } else if (!selected) {
                ShortToast.show(activity, R.string.toast_search_engine_invalid)
            }
        }
    }

    private fun addCustomSearchEngineButton(parent: LinearLayout) {
        host.contentFactory.addFunctionSection(parent, "") { section ->
            host.gridFactory.addActionGrid(
                section,
                listOf(
                    FunctionCenterGridAction(
                        title = activity.getString(R.string.action_add),
                        summary = activity.getString(R.string.action_add_custom_search_engine_summary),
                        iconResId = R.drawable.ic_add_24
                    ) {
                        showAddCustomSearchEngineDialog()
                    }
                )
            )
        }
    }

    private fun showAddCustomSearchEngineDialog() {
        TwoTextInputDialog.show(
            activity = activity,
            titleRes = R.string.title_add_custom_search_engine,
            message = activity.getString(R.string.dialog_add_custom_search_engine_message),
            firstField = TextInputDialogField(
                hintRes = R.string.hint_custom_search_engine_name,
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            ),
            secondField = TextInputDialogField(
                hintRes = R.string.hint_custom_search_engine_url_prefix,
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            ),
            positiveButtonRes = R.string.action_add,
            dp = ::dp
        ) { values ->
            val saved = settingsManager.addCustomSearchEngine(values.first, values.second)
            if (saved) {
                ShortToast.show(activity, R.string.toast_custom_search_engine_added)
                show(replaceCurrent = true)
            } else {
                ShortToast.show(activity, R.string.toast_custom_search_engine_invalid)
            }
            saved
        }
    }

    private fun dp(value: Int): Int {
        return DensityPixelConverter.truncateDp(value, activity.resources)
    }
}
