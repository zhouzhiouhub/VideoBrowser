package com.example.videobrowser.element

import android.os.SystemClock
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.browser.BrowserManager
import com.example.videobrowser.settings.SettingsManager

class ElementPickerController(
    private val activity: AppCompatActivity,
    private val browserManager: BrowserManager,
    private val settingsManager: SettingsManager,
    private val currentSiteHost: () -> String?,
    private val isJsInjectionEnabled: () -> Boolean,
    private val isCurrentSiteJsInjectionDisabled: () -> Boolean,
    private val injectPageFeatures: () -> Unit
) {
    private var startedAt = 0L
    private var dialog: AlertDialog? = null

    var isActive = false
        private set

    fun start() {
        val host = currentSiteHost()
        if (host == null) {
            Toast.makeText(activity, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        if (!isJsInjectionEnabled() || isCurrentSiteJsInjectionDisabled()) {
            Toast.makeText(
                activity,
                R.string.toast_element_picker_js_disabled,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (isActive) {
            finishSession()
        }
        isActive = true
        startedAt = SystemClock.elapsedRealtime()
        injectPageFeatures()
        browserManager.evaluateJavascript(START_ELEMENT_PICKER_SCRIPT)
        Toast.makeText(activity, R.string.toast_element_picker_started, Toast.LENGTH_SHORT).show()
    }

    fun cancel() {
        if (!isActive) {
            return
        }
        dialog?.dismiss()
        finishSession()
        Toast.makeText(activity, R.string.toast_element_picker_cancelled, Toast.LENGTH_SHORT).show()
    }

    fun handleCancelledFromPage() {
        if (!isActive) {
            return
        }
        finishSession()
        Toast.makeText(activity, R.string.toast_element_picker_cancelled, Toast.LENGTH_SHORT).show()
    }

    fun handlePickedElement(selector: String, description: String) {
        if (!isSessionValid()) {
            finishSession()
            return
        }

        val host = currentSiteHost() ?: run {
            finishSession()
            Toast.makeText(activity, R.string.toast_no_page_url, Toast.LENGTH_SHORT).show()
            return
        }
        showConfirmElementBlockDialog(host, selector, description)
    }

    fun clearState() {
        isActive = false
        startedAt = 0L
        dialog?.dismiss()
        dialog = null
    }

    fun dispose() {
        dialog?.dismiss()
        dialog = null
    }

    private fun showConfirmElementBlockDialog(host: String, selector: String, description: String) {
        val detail = listOf(description.trim(), selector.trim())
            .filter { value -> value.isNotBlank() }
            .distinct()
            .joinToString(separator = "\n")
        val activeDialog = AlertDialog.Builder(activity)
            .setTitle(R.string.title_confirm_element_block)
            .setMessage(activity.getString(R.string.dialog_confirm_element_block_message, host, detail))
            .setPositiveButton(R.string.action_block_element) { _, _ ->
                savePickedElement(host, selector)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                cancel()
            }
            .create()
        dialog?.dismiss()
        dialog = activeDialog
        activeDialog.setOnCancelListener {
            cancel()
        }
        activeDialog.setOnDismissListener {
            if (dialog === activeDialog) {
                dialog = null
            }
        }
        activeDialog.show()
    }

    private fun savePickedElement(host: String, selector: String) {
        val alreadySaved = settingsManager.hasUserElementHideSelectorForSite(host, selector)
        val saved = alreadySaved || settingsManager.addUserElementHideSelectorForSite(host, selector)
        finishSession()
        if (!saved) {
            Toast.makeText(activity, R.string.toast_element_picker_invalid, Toast.LENGTH_SHORT).show()
            return
        }

        injectPageFeatures()
        Toast.makeText(
            activity,
            if (alreadySaved) {
                R.string.toast_element_picker_already_saved
            } else {
                R.string.toast_element_picker_saved
            },
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun isSessionValid(): Boolean {
        return isActive &&
            SystemClock.elapsedRealtime() - startedAt <= ELEMENT_PICKER_TIMEOUT_MS
    }

    private fun finishSession() {
        clearState()
        browserManager.evaluateJavascript(FINISH_ELEMENT_PICKER_SCRIPT)
    }

    private companion object {
        private const val START_ELEMENT_PICKER_SCRIPT =
            "if(window.VideoBrowserEnhancer&&typeof window.VideoBrowserEnhancer.startElementPicker==='function'){" +
                "window.VideoBrowserEnhancer.startElementPicker();" +
                "}"
        private const val FINISH_ELEMENT_PICKER_SCRIPT =
            "if(window.VideoBrowserEnhancer&&typeof window.VideoBrowserEnhancer.finishElementPicker==='function'){" +
                "window.VideoBrowserEnhancer.finishElementPicker();" +
                "}"
        private const val ELEMENT_PICKER_TIMEOUT_MS = 60_000L
    }
}
