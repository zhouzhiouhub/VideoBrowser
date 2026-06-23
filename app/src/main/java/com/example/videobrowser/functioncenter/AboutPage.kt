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

    /**
     * 函数 `show`：控制 `show` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun show() {
        host.showPage(
            title = activity.getString(R.string.title_about),
            onBack = showProfilePage
        ) { content ->
            host.contentFactory.addFunctionSection(
                parent = content,
                title = activity.getString(R.string.function_center_section_about)
            ) { section ->
                host.contentFactory.addInfoRow(
                    parent = section,
                    title = activity.getString(R.string.about_version),
                    summary = BuildConfig.VERSION_NAME
                )
            }
        }
    }
}
