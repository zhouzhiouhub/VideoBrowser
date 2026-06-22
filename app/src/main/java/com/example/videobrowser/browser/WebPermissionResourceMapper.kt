package com.example.videobrowser.browser

import android.Manifest
import android.webkit.PermissionRequest
import com.example.videobrowser.R
import com.example.videobrowser.settings.SitePermission

/**
 * WebView PermissionRequest 资源映射模块。
 *
 * 控制器只负责权限流程，资源到 Android 权限、站点权限和展示文案的映射统一放在这里。
 */
object WebPermissionResourceMapper {
    fun supportedResources(resources: Array<String>): Array<String>? {
        val supportedResources = mutableListOf<String>()
        resources.forEach { resource ->
            when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                    if (resource !in supportedResources) {
                        supportedResources += resource
                    }
                }

                else -> return null
            }
        }
        return supportedResources.toTypedArray().takeIf { it.isNotEmpty() }
    }

    fun androidPermissionsFor(resources: Array<String>): List<String>? {
        val androidPermissions = mutableListOf<String>()
        resources.forEach { resource ->
            val permission = when (resource) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> Manifest.permission.CAMERA
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> Manifest.permission.RECORD_AUDIO
                else -> return null
            }
            if (permission !in androidPermissions) {
                androidPermissions += permission
            }
        }
        return androidPermissions.takeIf { it.isNotEmpty() }
    }

    fun sitePermissionsFor(resources: Array<String>): List<SitePermission> {
        return resources
            .mapNotNull(::sitePermissionFor)
            .distinct()
    }

    fun labelResourceIdFor(resource: String): Int? {
        return when (resource) {
            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> R.string.web_permission_camera
            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> R.string.web_permission_microphone
            else -> null
        }
    }

    private fun sitePermissionFor(resource: String): SitePermission? {
        return when (resource) {
            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> SitePermission.CAMERA
            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> SitePermission.MICROPHONE
            else -> null
        }
    }
}
