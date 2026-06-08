package com.example.videobrowser.functioncenter

import com.example.videobrowser.BuildConfig
import com.example.videobrowser.R

class AboutPage(
    private val host: FunctionCenterPageHost,
    private val showProfilePage: () -> Unit
) {
    private val activity = host.activity

    fun show() {
        host.showPage(
            title = activity.getString(R.string.title_about),
            onBack = showProfilePage
        ) { content ->
            host.addFunctionSection(
                parent = content,
                title = activity.getString(R.string.function_center_section_about)
            ) { section ->
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.about_app_name),
                    summary = activity.getString(R.string.app_name)
                )
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.about_version),
                    summary = BuildConfig.VERSION_NAME
                )
                host.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.about_git_commit_count),
                    summary = activity.getString(
                        R.string.about_git_commit_count_summary,
                        BuildConfig.GIT_COMMIT_COUNT
                    )
                )
            }
        }
    }
}
