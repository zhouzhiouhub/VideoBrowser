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
import com.example.videobrowser.storage.SavedPage

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
    private val viewFactory = FunctionCenterViewFactory(activity, dp)
    private var page: View? = null
    private var backAction: (() -> Unit)? = null
    private val pageHistory = FunctionCenterPageHistory<PageState>()

    private data class PageState(
        val view: View,
        val backAction: (() -> Unit)?
    )

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

    fun close(): Boolean {
        val currentPage = page ?: return false
        (currentPage.parent as? ViewGroup)?.removeView(currentPage)
        page = null
        backAction = null
        pageHistory.clear()
        return true
    }

    fun addFunctionSection(
        parent: LinearLayout,
        title: String,
        buildContent: (LinearLayout) -> Unit
    ) {
        viewFactory.addFunctionSection(parent, title, buildContent)
    }

    fun addInfoRow(parent: LinearLayout, title: String, summary: String) {
        viewFactory.addInfoRow(parent, title, summary)
    }

    fun addFunctionMessage(parent: LinearLayout, message: String) {
        viewFactory.addFunctionMessage(parent, message)
    }

    fun addProfileHeader(parent: LinearLayout, title: String, summary: String, onClick: () -> Unit) {
        viewFactory.addProfileHeader(parent, title, summary, onClick)
    }

    fun addBenefitStrip(
        parent: LinearLayout,
        leftTitle: String,
        leftSummary: String,
        rightTitle: String,
        rightSummary: String
    ) {
        viewFactory.addBenefitStrip(parent, leftTitle, leftSummary, rightTitle, rightSummary)
    }

    fun addHistoryPreview(
        parent: LinearLayout,
        title: String,
        emptyMessage: String,
        pages: List<SavedPage>,
        onOpenPage: (SavedPage) -> Unit,
        onShowHistory: () -> Unit
    ) {
        viewFactory.addHistoryPreview(parent, title, emptyMessage, pages, onOpenPage, onShowHistory)
    }

    fun addEmptyState(parent: LinearLayout, message: String) {
        viewFactory.addEmptyState(parent, message)
    }

    fun addFunctionActionButton(
        parent: LinearLayout,
        title: String,
        backgroundColor: Int? = null,
        onClick: () -> Unit
    ) {
        viewFactory.addFunctionActionButton(parent, title, backgroundColor, onClick)
    }

    fun addActionGrid(
        parent: LinearLayout,
        actions: List<FunctionCenterGridAction>
    ) {
        viewFactory.addActionGrid(parent, actions)
    }

    fun addSwitchRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        checked: Boolean,
        enabled: Boolean = true,
        onChanged: (Boolean) -> Unit
    ) {
        viewFactory.addSwitchRow(parent, title, summary, checked, enabled, onChanged)
    }

    fun addActionRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        enabled: Boolean = true,
        onClick: () -> Unit
    ) {
        viewFactory.addActionRow(parent, title, summary, enabled, onClick)
    }

    fun addDivider(parent: LinearLayout) {
        viewFactory.addDivider(parent)
    }

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

    private fun restorePage(pageState: PageState) {
        val container = rootView as? ViewGroup ?: return
        page?.let { currentPage ->
            (currentPage.parent as? ViewGroup)?.removeView(currentPage)
        }
        page = pageState.view
        backAction = pageState.backAction
        addPageToContainer(container, pageState.view)
    }

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
