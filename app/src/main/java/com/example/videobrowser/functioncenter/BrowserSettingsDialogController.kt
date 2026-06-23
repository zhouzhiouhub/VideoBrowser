package com.example.videobrowser.functioncenter

import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.search.SearchProviders
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.utils.ValidatedTextInputDialog

class BrowserSettingsDialogController(
    private val activity: AppCompatActivity,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val selectSearchProvider: (String) -> Boolean,
    private val onSettingsChanged: () -> Unit
) {
    fun showHomeUrlDialog() {
        ValidatedTextInputDialog.show(
            activity = activity,
            titleRes = R.string.setting_home_page,
            hintRes = R.string.hint_home_page_url,
            initialValue = settingsManager.homeUrl(),
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI,
            positiveButtonRes = R.string.action_save,
            invalidToastRes = R.string.toast_home_page_invalid,
            successToastRes = R.string.toast_home_page_updated,
            saveValue = { homeUrl ->
                if (!settingsManager.isValidHomeUrl(homeUrl)) {
                    false
                } else {
                    settingsManager.setHomeUrl(homeUrl)
                    true
                }
            },
            onSaved = {
                onSettingsChanged()
            }
        )
    }

    fun showSearchEngineDialog() {
        val providers = SearchProviders.defaults
        val selectedIndex = providers
            .indexOfFirst { provider -> provider.id == settingsManager.searchEngineId() }
            .takeIf { index -> index >= 0 }
            ?: 0
        SingleChoiceDialog.show(
            activity = activity,
            titleRes = R.string.setting_search_engine,
            labels = providers.map { provider -> provider.name },
            checkedIndex = selectedIndex
        ) { index ->
            val provider = providers[index]
            val toastResId = if (selectSearchProvider(provider.id)) {
                R.string.toast_search_engine_updated
            } else {
                R.string.toast_search_engine_invalid
            }
            Toast.makeText(activity, toastResId, Toast.LENGTH_SHORT).show()
            onSettingsChanged()
        }
    }

    fun showTextZoomDialog() {
        val options = SettingsManager.TEXT_ZOOM_OPTIONS
        val selectedIndex = options
            .indexOf(settingsManager.textZoomPercent())
            .takeIf { index -> index >= 0 }
            ?: options.indexOf(SettingsManager.DEFAULT_TEXT_ZOOM_PERCENT).coerceAtLeast(0)
        SingleChoiceDialog.show(
            activity = activity,
            titleRes = R.string.setting_text_zoom,
            labels = options.map { percent ->
                activity.getString(R.string.text_zoom_percent, percent)
            },
            checkedIndex = selectedIndex
        ) { index ->
            val percent = options[index]
            settingsManager.setTextZoomPercent(percent)
            browserManager().setTextZoomPercent(percent)
            Toast.makeText(activity, R.string.toast_text_zoom_updated, Toast.LENGTH_SHORT).show()
            onSettingsChanged()
        }
    }
}
