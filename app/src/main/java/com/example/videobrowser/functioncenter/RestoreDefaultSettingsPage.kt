package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 RestoreDefaultSettingsPage 可以拆开理解为“Restore Default Settings Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import androidx.appcompat.app.AlertDialog
import com.example.videobrowser.R

class RestoreDefaultSettingsPage(
    private val host: FunctionCenterPageHost,
    private val restoreDefaultSettings: () -> Unit
) {
    private val activity = host.activity

    fun show() {
        AlertDialog.Builder(activity)
            .setTitle(R.string.action_restore_default_settings)
            .setMessage(R.string.dialog_restore_default_settings_message)
            .setPositiveButton(R.string.action_restore) { _, _ -> restoreDefaultSettings() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
