package com.example.videobrowser.functioncenter

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.videobrowser.storage.SavedPage

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
