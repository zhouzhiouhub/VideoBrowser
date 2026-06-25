package com.example.videobrowser.functioncenter

import android.text.InputType
import com.example.videobrowser.R
import com.example.videobrowser.browser.search.CustomSearchEngineInputResolver
import com.example.videobrowser.browser.search.SearchEngineConfig
import com.example.videobrowser.settings.CustomSearchEngine
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.utils.ConfirmationDialog
import com.example.videobrowser.utils.DensityPixelConverter
import com.example.videobrowser.utils.ShortToast
import com.example.videobrowser.utils.TextInputDialogField
import com.example.videobrowser.utils.TwoTextInputDialog

/**
 * 单个自定义搜索引擎的管理页，负责设为默认、编辑和移除。
 */
internal class CustomSearchEngineSettingsPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val currentSearchProviderId: () -> String,
    private val selectSearchProvider: (String) -> Boolean,
    private val showSearchEngineSettingsPage: () -> Unit
) {
    private val activity = host.activity

    fun show(engine: CustomSearchEngine, replaceCurrent: Boolean = false) {
        val currentEngine = findCurrentEngine(engine)
        if (currentEngine == null) {
            showSearchEngineSettingsPage()
            return
        }
        val selected = currentSearchProviderId() == currentEngine.id
        host.showPage(
            title = currentEngine.name,
            onBack = showSearchEngineSettingsPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            host.contentFactory.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_settings)
            ) { section ->
                host.contentFactory.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.custom_search_engine_url_prefix),
                    summary = currentEngine.displayUrl
                )
                host.contentFactory.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_set_default_search_engine),
                    summary = if (selected) {
                        activity.getString(
                            R.string.search_engine_selected_summary,
                            currentEngine.displayUrl
                        )
                    } else {
                        activity.getString(R.string.action_set_default_search_engine_summary)
                    },
                    enabled = !selected
                ) {
                    selectDefaultSearchEngine(currentEngine)
                }
            }
            host.contentFactory.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.contentFactory.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_edit),
                    summary = activity.getString(R.string.action_edit_custom_search_engine_summary)
                ) {
                    showEditCustomSearchEngineDialog(currentEngine)
                }
                host.contentFactory.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_remove),
                    summary = activity.getString(R.string.action_remove_custom_search_engine_summary)
                ) {
                    showRemoveCustomSearchEngineDialog(currentEngine)
                }
            }
        }
    }

    private fun selectDefaultSearchEngine(engine: CustomSearchEngine) {
        if (selectSearchProvider(engine.id)) {
            ShortToast.show(activity, R.string.toast_search_engine_updated)
            show(engine, replaceCurrent = true)
        } else {
            ShortToast.show(activity, R.string.toast_search_engine_invalid)
        }
    }

    private fun showEditCustomSearchEngineDialog(engine: CustomSearchEngine) {
        TwoTextInputDialog.show(
            activity = activity,
            titleRes = R.string.title_edit_custom_search_engine,
            message = activity.getString(R.string.dialog_add_custom_search_engine_message),
            firstField = TextInputDialogField(
                hintRes = R.string.hint_custom_search_engine_name,
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                initialValue = engine.name
            ),
            secondField = TextInputDialogField(
                hintRes = R.string.hint_custom_search_engine_url_prefix,
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI,
                initialValue = engine.displayUrl
            ),
            positiveButtonRes = R.string.action_save,
            dp = ::dp
        ) { values ->
            val saved = CustomSearchEngineInputResolver.resolve(values.second)
                ?.let { config -> updateCustomSearchEngine(engine, values.first, config) }
                ?: false
            if (saved) {
                val updatedEngine = findCurrentEngine(engine)
                if (updatedEngine != null && currentSearchProviderId() == updatedEngine.id) {
                    selectSearchProvider(updatedEngine.id)
                }
                ShortToast.show(activity, R.string.toast_custom_search_engine_updated)
                if (updatedEngine == null) {
                    showSearchEngineSettingsPage()
                } else {
                    show(updatedEngine, replaceCurrent = true)
                }
            } else {
                ShortToast.show(activity, R.string.toast_custom_search_engine_invalid)
            }
            saved
        }
    }

    private fun updateCustomSearchEngine(
        engine: CustomSearchEngine,
        name: String,
        config: SearchEngineConfig
    ): Boolean {
        return settingsManager.updateCustomSearchEngine(
            engine = engine,
            name = name,
            displayUrl = config.displayUrl,
            searchTemplate = config.searchTemplate,
            queryParam = config.queryParam,
            domains = config.domains,
            hideCss = config.hideCss,
            hidePageSearchBox = config.hidePageSearchBox
        )
    }

    private fun showRemoveCustomSearchEngineDialog(engine: CustomSearchEngine) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.title_remove_custom_search_engine,
            message = activity.getString(
                R.string.dialog_remove_custom_search_engine_message,
                engine.name
            ),
            positiveButtonRes = R.string.action_remove
        ) {
            val wasSelected = currentSearchProviderId() == engine.id
            if (settingsManager.removeCustomSearchEngine(engine)) {
                if (wasSelected) {
                    selectSearchProvider(SettingsManager.DEFAULT_SEARCH_ENGINE_ID)
                }
                ShortToast.show(activity, R.string.toast_custom_search_engine_removed)
                showSearchEngineSettingsPage()
            } else {
                ShortToast.show(activity, R.string.toast_custom_search_engine_invalid)
            }
        }
    }

    private fun findCurrentEngine(engine: CustomSearchEngine): CustomSearchEngine? {
        return settingsManager.customSearchEngines().firstOrNull { currentEngine ->
            currentEngine.id == engine.id
        }
    }

    private fun dp(value: Int): Int {
        return DensityPixelConverter.truncateDp(value, activity.resources)
    }
}
