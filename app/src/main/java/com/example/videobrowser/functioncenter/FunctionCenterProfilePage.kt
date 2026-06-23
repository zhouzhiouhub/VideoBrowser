package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import com.example.videobrowser.R

internal class FunctionCenterProfilePage(
    private val host: FunctionCenterPageHost,
    private val profileShortcutSection: FunctionCenterProfileShortcutSection,
    private val browserSettingsPage: BrowserSettingsPage,
    private val closePage: () -> Unit
) {
    private val activity = host.activity

    fun show() {
        host.showPage(
            title = activity.getString(R.string.title_profile_page),
            onBack = closePage
        ) { content ->
            FunctionCenterProfilePageLayout.blocks().forEach { block ->
                when (block) {
                    FunctionCenterProfilePageBlock.PROFILE_HEADER -> addProfileHeader(content)
                    FunctionCenterProfilePageBlock.SHORTCUTS -> profileShortcutSection.add(content)
                    FunctionCenterProfilePageBlock.FEATURES -> addProfileFeatureSection(content)
                }
            }
        }
    }

    private fun addProfileHeader(parent: LinearLayout) {
        host.headerFactory.addProfileHeader(
            parent = parent,
            title = activity.getString(R.string.function_center_profile_name),
            summary = activity.getString(R.string.function_center_profile_summary)
        ) {
            browserSettingsPage.show()
        }
    }

    private fun addProfileFeatureSection(parent: LinearLayout) {
        browserSettingsPage.addExpandedBrowserSettings(parent)
        browserSettingsPage.addProfileDataManagement(parent)
    }
}
