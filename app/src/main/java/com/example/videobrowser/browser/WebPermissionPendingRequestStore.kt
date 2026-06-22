package com.example.videobrowser.browser

import android.webkit.PermissionRequest

internal class WebPermissionPendingRequestStore {
    private var pendingWebPermissionRequest: PermissionRequest? = null

    fun replaceWith(request: PermissionRequest) {
        pendingWebPermissionRequest?.deny()
        pendingWebPermissionRequest = request
    }

    fun take(): PermissionRequest? {
        val request = pendingWebPermissionRequest
        pendingWebPermissionRequest = null
        return request
    }

    fun clearIfPending(request: PermissionRequest) {
        if (request == pendingWebPermissionRequest) {
            pendingWebPermissionRequest = null
        }
    }

    fun cancelPending() {
        pendingWebPermissionRequest?.deny()
        pendingWebPermissionRequest = null
    }
}
