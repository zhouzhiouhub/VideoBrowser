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
        val supportedResources = resources
            .map { resource ->
                resourceDefinitionFor(resource)?.resource ?: return null
            }
            .distinct()
        return supportedResources.toTypedArray().takeIf { it.isNotEmpty() }
    }

    fun androidPermissionsFor(resources: Array<String>): List<String>? {
        val androidPermissions = resources
            .map { resource ->
                resourceDefinitionFor(resource)?.androidPermission ?: return null
            }
            .distinct()
        return androidPermissions.takeIf { it.isNotEmpty() }
    }

    fun sitePermissionsFor(resources: Array<String>): List<SitePermission> {
        return resources
            .mapNotNull { resource -> resourceDefinitionFor(resource)?.sitePermission }
            .distinct()
    }

    fun labelResourceIdFor(resource: String): Int? {
        return resourceDefinitionFor(resource)?.labelResourceId
    }

    private fun resourceDefinitionFor(resource: String): WebPermissionResourceDefinition? {
        return resourceDefinitionsByResource[resource]
    }

    private data class WebPermissionResourceDefinition(
        val resource: String,
        val androidPermission: String,
        val sitePermission: SitePermission,
        val labelResourceId: Int
    )

    private val resourceDefinitions = listOf(
        WebPermissionResourceDefinition(
            resource = PermissionRequest.RESOURCE_VIDEO_CAPTURE,
            androidPermission = Manifest.permission.CAMERA,
            sitePermission = SitePermission.CAMERA,
            labelResourceId = R.string.web_permission_camera
        ),
        WebPermissionResourceDefinition(
            resource = PermissionRequest.RESOURCE_AUDIO_CAPTURE,
            androidPermission = Manifest.permission.RECORD_AUDIO,
            sitePermission = SitePermission.MICROPHONE,
            labelResourceId = R.string.web_permission_microphone
        )
    )

    private val resourceDefinitionsByResource = resourceDefinitions.associateBy { definition ->
        definition.resource
    }
}
