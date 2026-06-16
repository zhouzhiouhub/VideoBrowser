package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 AboutPage 可以拆开理解为“About Page”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
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
                    title = activity.getString(R.string.about_version),
                    summary = BuildConfig.VERSION_NAME
                )
            }
        }
    }
}
