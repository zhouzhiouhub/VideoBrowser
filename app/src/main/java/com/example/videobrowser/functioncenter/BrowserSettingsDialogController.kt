package com.example.videobrowser.functioncenter

import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.utils.ShortToast

class BrowserSettingsDialogController(
    private val activity: AppCompatActivity,
    private val settingsManager: SettingsManager,
    private val browserManager: () -> BrowserManager,
    private val onSettingsChanged: () -> Unit
) {
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
            ShortToast.show(activity, R.string.toast_text_zoom_updated)
            onSettingsChanged()
        }
    }
}
