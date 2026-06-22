package com.example.videobrowser.browser

internal class BrowserAndroidPermissionGate<T>(
    private val hasAndroidPermission: (String) -> Boolean,
    private val requestAndroidPermissions: (Array<String>) -> Unit,
    private val requiredPermissionsFor: (T) -> Array<String>?,
    private val resultPolicy: BrowserAndroidPermissionResultPolicy,
    private val replacePendingRequest: (T) -> Unit,
    private val takePendingRequest: () -> T?,
    private val continueAfterPermission: (T) -> Unit,
    private val denyRequest: (T) -> Unit
) {
    fun continueOrRequest(request: T) {
        val requiredPermissions = requiredPermissionsFor(request)
        if (requiredPermissions == null) {
            denyRequest(request)
            return
        }
        if (resultPolicy.isSatisfied(requiredPermissions, emptyMap(), hasAndroidPermission)) {
            continueAfterPermission(request)
            return
        }

        replacePendingRequest(request)
        requestAndroidPermissions(
            requiredPermissions
                .filterNot(hasAndroidPermission)
                .toTypedArray()
        )
    }

    fun handleResult(grants: Map<String, Boolean>) {
        val request = takePendingRequest() ?: return
        val requiredPermissions = requiredPermissionsFor(request)
        if (requiredPermissions != null &&
            resultPolicy.isSatisfied(requiredPermissions, grants, hasAndroidPermission)
        ) {
            continueAfterPermission(request)
        } else {
            denyRequest(request)
        }
    }
}

internal enum class BrowserAndroidPermissionResultPolicy {
    ALL_REQUIRED,
    ANY_REQUIRED;

    fun isSatisfied(
        permissions: Array<String>,
        grants: Map<String, Boolean>,
        hasAndroidPermission: (String) -> Boolean
    ): Boolean {
        return when (this) {
            ALL_REQUIRED -> permissions.all { permission ->
                grants[permission] == true || hasAndroidPermission(permission)
            }
            ANY_REQUIRED -> permissions.any { permission ->
                grants[permission] == true || hasAndroidPermission(permission)
            }
        }
    }
}
