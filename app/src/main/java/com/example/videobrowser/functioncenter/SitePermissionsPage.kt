package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 SitePermissionsPage 可以拆开理解为“Site Permissions Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import com.example.videobrowser.R
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.settings.SitePermissionDecision
import com.example.videobrowser.settings.SitePermissionRecord
import com.example.videobrowser.utils.ConfirmationDialog
import com.example.videobrowser.utils.ShortToast

class SitePermissionsPage(
    private val host: FunctionCenterPageHost,
    private val settingsManager: SettingsManager,
    private val showRootPage: () -> Unit
) {
    private val activity = host.activity
    private val sitePermissionTextFormatter = SitePermissionTextFormatter(activity)

    /**
     * 函数 `show`：控制 `show` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     */
    fun show(replaceCurrent: Boolean = false) {
        val records = settingsManager.sitePermissionRecords()
            .sortedWith(
                compareBy<SitePermissionRecord> { record -> record.host }
                    .thenBy { record -> record.permission.name }
                    .thenBy { record -> record.decision.name }
            )

        host.showPage(
            title = activity.getString(R.string.title_site_permissions),
            onBack = showRootPage,
            replaceCurrent = replaceCurrent
        ) { content ->
            if (records.isEmpty()) {
                host.contentFactory.addEmptyState(content, activity.getString(R.string.dialog_site_permissions_empty))
                return@showPage
            }

            host.contentFactory.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_actions)
            ) { section ->
                host.contentFactory.addActionRow(
                    parent = section,
                    title = activity.getString(R.string.action_clear),
                    summary = activity.getString(R.string.action_clear_site_permissions_summary)
                ) {
                    showClearSitePermissionsDialog()
                }
            }

            host.contentFactory.addFunctionSection(
                content,
                activity.getString(R.string.function_center_section_records)
            ) { section ->
                records.forEach { record ->
                    host.contentFactory.addActionRow(
                        parent = section,
                        title = record.host,
                        summary = sitePermissionTextFormatter.recordSummary(record)
                    ) {
                        showRemoveSitePermissionDialog(record)
                    }
                }
            }
        }
    }

    /**
     * 函数 `showRemoveSitePermissionDialog`：控制 `show Remove Site Permission Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `SitePermissionRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     */
    private fun showRemoveSitePermissionDialog(record: SitePermissionRecord) {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.title_remove_site_permission,
            message = activity.getString(
                R.string.dialog_remove_site_permission_message,
                record.host,
                sitePermissionTextFormatter.title(record.permission),
                sitePermissionTextFormatter.decisionText(record.decision)
            ),
            positiveButtonRes = R.string.action_remove
        ) {
            settingsManager.setSitePermissionDecision(
                record.host,
                record.permission,
                SitePermissionDecision.ASK
            )
            ShortToast.show(activity, R.string.toast_site_permission_removed)
            show(replaceCurrent = true)
        }
    }

    /**
     * 函数 `showClearSitePermissionsDialog`：控制 `show Clear Site Permissions Dialog` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showClearSitePermissionsDialog() {
        ConfirmationDialog.show(
            activity = activity,
            titleRes = R.string.action_clear,
            messageRes = R.string.dialog_clear_site_permissions_message,
            positiveButtonRes = R.string.action_clear
        ) {
            settingsManager.clearSitePermissionDecisions()
            ShortToast.show(activity, R.string.toast_site_permissions_cleared)
            show(replaceCurrent = true)
        }
    }

}
