package com.example.videobrowser.browser.search

import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.settings.CustomShortcut
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.SavedPageRepository
import com.example.videobrowser.utils.ActionListDialog
import com.example.videobrowser.utils.ConfirmationDialog
import com.example.videobrowser.utils.DialogAction
import com.example.videobrowser.utils.TextInputDialogField
import com.example.videobrowser.utils.TwoTextInputDialog

internal class SearchProviderDialogController(
    private val activity: AppCompatActivity,
    private val settingsManager: SettingsManager,
    private val savedPageRepository: SavedPageRepository,
    private val dp: (Int) -> Int,
    private val onDataChanged: () -> Unit
) {
    fun showAddShortcutDialog() {
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

    fun showCustomShortcutActionsDialog(shortcut: CustomShortcut) {
        val actions = listOf(
            DialogAction(activity.getString(R.string.action_edit)) {
                showEditCustomShortcutDialog(shortcut)
            },
            DialogAction(activity.getString(R.string.action_remove)) {
                showRemoveCustomShortcutDialog(shortcut)
            }
        )
        ActionListDialog.show(
            activity = activity,
            title = shortcut.name,
            actions = actions
        )
    }

    fun showEditCustomShortcutDialog(shortcut: CustomShortcut) {
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

    fun showRemoveCustomShortcutDialog(shortcut: CustomShortcut) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.title_remove_custom_shortcut,
            message = activity.getString(
                R.string.dialog_remove_custom_shortcut_message,
                shortcut.name
            ),
            positiveButtonRes = R.string.action_remove
        ) {
            if (settingsManager.removeCustomShortcut(shortcut)) {
                onDataChanged()
                Toast.makeText(
                    activity,
                    R.string.toast_custom_shortcut_removed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun showRemoveRecentHistoryDialog(quickLink: HomeQuickLink) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.title_remove_recent_site,
            message = activity.getString(
                R.string.dialog_remove_recent_site_message,
                quickLink.title
            ),
            positiveButtonRes = R.string.action_remove
        ) {
            val removed = savedPageRepository.remove(
                SavedPageRepository.SavedPageCollection.HISTORY,
                quickLink.url
            )
            if (removed) {
                onDataChanged()
                Toast.makeText(
                    activity,
                    R.string.toast_recent_site_removed,
                    Toast.LENGTH_SHORT
                ).show()
            }
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
        TwoTextInputDialog.show(
            activity = activity,
            titleRes = titleResId,
            firstField = TextInputDialogField(
                hintRes = R.string.hint_custom_shortcut_name,
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                initialValue = initialName
            ),
            secondField = TextInputDialogField(
                hintRes = R.string.hint_custom_shortcut_url,
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI,
                initialValue = initialUrl
            ),
            positiveButtonRes = positiveButtonResId,
            dp = dp
        ) { values ->
            val saved = saveShortcut(values.first, values.second)
            if (saved) {
                onDataChanged()
                Toast.makeText(
                    activity,
                    successToastResId,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    activity,
                    R.string.toast_custom_shortcut_invalid,
                    Toast.LENGTH_SHORT
                ).show()
            }
            saved
        }
    }
}
