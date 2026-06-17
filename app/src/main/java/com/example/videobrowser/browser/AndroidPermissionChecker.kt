package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“Android 运行时权限模块”。
 * WebView 的相机、麦克风和定位权限都要先检查 Android 系统权限；
 * 本类把 ContextCompat/PackageManager 的系统 API 调用从 MainActivity 中拆出来。
 */
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Android 运行时权限检查器。
 *
 * @param context 参数类型为 `Context`，表示用于访问 Android 权限状态的上下文对象。
 */
class AndroidPermissionChecker(
    private val context: Context
) {
    /**
     * 判断指定 Android 权限是否已经授予。
     *
     * @param permission 参数类型为 `String`，表示要检查的 Android 权限名称，例如 Manifest.permission.CAMERA。
     * @return true 表示权限已经授予，false 表示尚未授予或被系统拒绝。
     */
    fun hasAndroidPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
