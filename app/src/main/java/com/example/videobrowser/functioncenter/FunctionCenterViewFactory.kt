package com.example.videobrowser.functioncenter

import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * 功能中心 View 工厂。
 *
 * 项目没有使用 XML 编写这些弹层页面，而是在 Kotlin 中动态创建 View。
 * 这个类只装配页面容器和复用的子工厂；具体组件创建逻辑保留在各自工厂里。
 */
class FunctionCenterViewFactory(
    activity: AppCompatActivity,
    dp: (Int) -> Int
) {
    private val surfaceFactory = FunctionCenterSurfaceFactory(activity, dp)
    private val pageFactory = FunctionCenterPageViewFactory(activity, dp, surfaceFactory)
    private val rowFactory = FunctionCenterRowFactory(activity, dp, surfaceFactory)
    internal val headerFactory = FunctionCenterHeaderFactory(
        activity,
        dp,
        surfaceFactory,
        rowFactory
    )
    internal val contentFactory = FunctionCenterContentFactory(
        activity,
        dp,
        surfaceFactory,
        rowFactory
    )
    internal val gridFactory = FunctionCenterGridFactory(activity, dp)

    fun createPage(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ): View {
        return pageFactory.createPage(title, onBack, buildContent)
    }

    fun createPageWithFooter(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit,
        buildFooter: (LinearLayout) -> Unit
    ): View {
        return pageFactory.createPageWithFooter(title, onBack, buildContent, buildFooter)
    }

    fun createBottomSheetPage(
        title: String,
        onBack: (() -> Unit)?,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ): View {
        return pageFactory.createBottomSheetPage(title, onBack, onClose, buildContent)
    }
}
