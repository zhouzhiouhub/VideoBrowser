package com.example.videobrowser.functioncenter

import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.browser.search.SearchProviders
import com.example.videobrowser.settings.SettingsManager

class BrowserSettingsDialogController(
    private val activity: AppCompatActivity,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val selectSearchProvider: (String) -> Boolean,
    private val onSettingsChanged: () -> Unit
) {
    fun showHomeUrlDialog() {
        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine(true)
            hint = activity.getString(R.string.hint_home_page_url)
            setText(settingsManager.homeUrl())
            setSelection(text?.length ?: 0)
        }
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.setting_home_page)
            .setView(input)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val homeUrl = input.text?.toString().orEmpty()
                if (!settingsManager.isValidHomeUrl(homeUrl)) {
                    Toast.makeText(activity, R.string.toast_home_page_invalid, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                settingsManager.setHomeUrl(homeUrl)
                Toast.makeText(activity, R.string.toast_home_page_updated, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                onSettingsChanged()
            }
        }
        dialog.show()
    }

    fun showSearchEngineDialog() {
        val providers = SearchProviders.defaults
        val selectedIndex = providers
            .indexOfFirst { provider -> provider.id == settingsManager.searchEngineId() }
            .takeIf { index -> index >= 0 }
            ?: 0
        AlertDialog.Builder(activity)
            .setTitle(R.string.setting_search_engine)
            .setSingleChoiceItems(
                providers.map { provider -> provider.name }.toTypedArray(),
                selectedIndex
            ) { dialog, index ->
                val provider = providers[index]
                val toastResId = if (selectSearchProvider(provider.id)) {
                    R.string.toast_search_engine_updated
                } else {
                    R.string.toast_search_engine_invalid
                }
                Toast.makeText(activity, toastResId, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                onSettingsChanged()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    fun showTextZoomDialog() {
        val options = SettingsManager.TEXT_ZOOM_OPTIONS
        val selectedIndex = options
            .indexOf(settingsManager.textZoomPercent())
            .takeIf { index -> index >= 0 }
            ?: options.indexOf(SettingsManager.DEFAULT_TEXT_ZOOM_PERCENT).coerceAtLeast(0)
        AlertDialog.Builder(activity)
            .setTitle(R.string.setting_text_zoom)
            .setSingleChoiceItems(
                options.map { percent ->
                    activity.getString(R.string.text_zoom_percent, percent)
                }.toTypedArray(),
                selectedIndex
            ) { dialog, index ->
                val percent = options[index]
                settingsManager.setTextZoomPercent(percent)
                browserManager().setTextZoomPercent(percent)
                Toast.makeText(activity, R.string.toast_text_zoom_updated, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                onSettingsChanged()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
