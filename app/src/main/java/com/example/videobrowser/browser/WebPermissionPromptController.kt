package com.example.videobrowser.browser

import android.webkit.PermissionRequest
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R

class WebPermissionPromptController(
    private val activity: AppCompatActivity,
    private val saveDecision: (PermissionRequest, Boolean) -> Unit,
    private val allowForSession: (PermissionRequest) -> Unit,
    private val grantSupportedResources: (PermissionRequest) -> Unit
) {
    private var pendingWebPermissionPromptRequest: PermissionRequest? = null
    private var pendingWebPermissionDialog: AlertDialog? = null

    fun show(request: PermissionRequest) {
        cancelPendingPrompt()
        pendingWebPermissionPromptRequest = request
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.title_web_permission_request)
            .setMessage(
                activity.getString(
                    R.string.dialog_web_permission_request_message,
                    webPermissionOrigin(request),
                    webPermissionResourceSummary(request.resources)
                )
            )
            .setPositiveButton(R.string.action_allow) { _, _ ->
                answerPermissionPrompt(request, allowed = true)
            }
            .setNeutralButton(R.string.action_allow_once) { _, _ ->
                answerPermissionPrompt(request, allowed = true, rememberDecision = false)
            }
            .setNegativeButton(R.string.action_deny) { _, _ ->
                answerPermissionPrompt(request, allowed = false)
            }
            .create()
        dialog.setOnCancelListener {
            answerPermissionPrompt(request, allowed = false)
        }
        pendingWebPermissionDialog = dialog
        dialog.show()
    }

    fun cancelIfPending(request: PermissionRequest): Boolean {
        if (request != pendingWebPermissionPromptRequest) {
            return false
        }
        cancelPendingPrompt()
        return true
    }

    fun cancelPendingPrompt() {
        val request = pendingWebPermissionPromptRequest
        pendingWebPermissionPromptRequest = null
        pendingWebPermissionDialog?.dismiss()
        pendingWebPermissionDialog = null
        request?.deny()
    }

    private fun answerPermissionPrompt(
        request: PermissionRequest,
        allowed: Boolean,
        rememberDecision: Boolean = true
    ) {
        if (pendingWebPermissionPromptRequest != request) {
            return
        }
        pendingWebPermissionPromptRequest = null
        pendingWebPermissionDialog = null
        if (allowed) {
            if (rememberDecision) {
                saveDecision(request, true)
            } else {
                allowForSession(request)
            }
            grantSupportedResources(request)
        } else {
            if (rememberDecision) {
                saveDecision(request, false)
            }
            request.deny()
        }
    }

    private fun webPermissionOrigin(request: PermissionRequest): String {
        return request.origin
            ?.toString()
            ?.takeIf { origin -> origin.isNotBlank() }
            ?: activity.getString(R.string.permission_origin_unknown)
    }

    private fun webPermissionResourceSummary(resources: Array<String>): String {
        return resources
            .mapNotNull { resource ->
                WebPermissionResourceMapper.labelResourceIdFor(resource)
                    ?.let(activity::getString)
            }
            .distinct()
            .joinToString(", ")
    }
}
