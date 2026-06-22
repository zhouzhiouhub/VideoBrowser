package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 FunctionCenterController 可以拆开理解为“Function Center Controller”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * 功能中心容器控制器。
 *
 * 它负责把页面 View 挂到 MainActivity 根布局上，并维护一个轻量页面栈。
 * 页面内容本身由 FunctionCenterViewFactory 和具体页面类创建。
 */
class FunctionCenterController(
    activity: AppCompatActivity,
    private val rootView: View,
    dp: (Int) -> Int
) {
    internal val viewFactory = FunctionCenterViewFactory(activity, dp)
    private var page: View? = null
    private var backAction: (() -> Unit)? = null
    private val pageHistory = FunctionCenterPageHistory<PageState>()

    private data class PageState(
        val view: View,
        val backAction: (() -> Unit)?
    )

    /**
     * 函数 `showPage`：控制 `show Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onBack 参数类型为 `() -> Unit`，表示函数执行 `onBack` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     */
    fun showPage(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        attachPage(
            viewFactory.createPage(title, { handleBack() }, buildContent),
            onBack,
            saveCurrentPage = true
        )
    }

    /**
     * 函数 `replacePage`：封装 `replace Page` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onBack 参数类型为 `() -> Unit`，表示函数执行 `onBack` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     */
    fun replacePage(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        attachPage(
            viewFactory.createPage(title, { handleBack() }, buildContent),
            onBack,
            saveCurrentPage = false
        )
    }

    /**
     * 函数 `showBottomSheetPage`：控制 `show Bottom Sheet Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onClose 参数类型为 `() -> Unit`，表示函数执行 `onClose` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     */
    fun showBottomSheetPage(
        title: String,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        pageHistory.clear()
        attachPage(
            viewFactory.createBottomSheetPage(title, null, onClose, buildContent),
            onClose,
            saveCurrentPage = false
        )
    }

    /**
     * 函数 `showBottomSheetPage`：控制 `show Bottom Sheet Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onBack 参数类型为 `() -> Unit`，表示函数执行 `onBack` 相关逻辑时需要读取或处理的输入。
     * @param onClose 参数类型为 `() -> Unit`，表示函数执行 `onClose` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     */
    fun showBottomSheetPage(
        title: String,
        onBack: () -> Unit,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        attachPage(
            viewFactory.createBottomSheetPage(title, { handleBack() }, onClose, buildContent),
            onBack,
            saveCurrentPage = true
        )
    }

    /**
     * 函数 `replaceBottomSheetPage`：封装 `replace Bottom Sheet Page` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onBack 参数类型为 `() -> Unit`，表示函数执行 `onBack` 相关逻辑时需要读取或处理的输入。
     * @param onClose 参数类型为 `() -> Unit`，表示函数执行 `onClose` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     */
    fun replaceBottomSheetPage(
        title: String,
        onBack: () -> Unit,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        attachPage(
            viewFactory.createBottomSheetPage(title, { handleBack() }, onClose, buildContent),
            onBack,
            saveCurrentPage = false
        )
    }

    /**
     * 函数 `handleBack`：处理 `handle Back` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun handleBack(): Boolean {
        if (page == null) {
            return false
        }
        val previousPage = pageHistory.pop()
        if (previousPage != null) {
            restorePage(previousPage)
            return true
        }
        backAction?.invoke() ?: close()
        return true
    }

    /**
     * 函数 `close`：控制 `close` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun close(): Boolean {
        val currentPage = page ?: return false
        (currentPage.parent as? ViewGroup)?.removeView(currentPage)
        page = null
        backAction = null
        pageHistory.clear()
        return true
    }

    /**
     * 函数 `attachPage`：封装 `attach Page` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param page 参数类型为 `View`，表示函数执行 `page` 相关逻辑时需要读取或处理的输入。
     * @param onBack 参数类型为 `() -> Unit`，表示函数执行 `onBack` 相关逻辑时需要读取或处理的输入。
     * @param saveCurrentPage 参数类型为 `Boolean`，表示函数执行 `saveCurrentPage` 相关逻辑时需要读取或处理的输入。
     */
    private fun attachPage(page: View, onBack: () -> Unit, saveCurrentPage: Boolean) {
        // saveCurrentPage 为 true 时把当前页压栈，这样功能中心内部的返回键能回到上一页。
        val container = rootView as? ViewGroup ?: return
        this.page?.let { currentPage ->
            (currentPage.parent as? ViewGroup)?.removeView(currentPage)
            if (saveCurrentPage) {
                pageHistory.push(PageState(currentPage, backAction))
            }
        }

        this.page = page
        backAction = onBack
        addPageToContainer(container, page)
    }

    /**
     * 函数 `restorePage`：封装 `restore Page` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageState 参数类型为 `PageState`，表示函数执行 `pageState` 相关逻辑时需要读取或处理的输入。
     */
    private fun restorePage(pageState: PageState) {
        val container = rootView as? ViewGroup ?: return
        page?.let { currentPage ->
            (currentPage.parent as? ViewGroup)?.removeView(currentPage)
        }
        page = pageState.view
        backAction = pageState.backAction
        addPageToContainer(container, pageState.view)
    }

    /**
     * 函数 `addPageToContainer`：封装 `add Page To Container` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param container 参数类型为 `ViewGroup`，表示函数执行 `container` 相关逻辑时需要读取或处理的输入。
     * @param page 参数类型为 `View`，表示函数执行 `page` 相关逻辑时需要读取或处理的输入。
     */
    private fun addPageToContainer(container: ViewGroup, page: View) {
        container.addView(
            page,
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
        )
        page.bringToFront()
        page.requestFocus()
    }
}
