package com.example.videobrowser.functioncenter

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class FunctionCenterController(
    activity: AppCompatActivity,
    private val rootView: View,
    dp: (Int) -> Int
) {
    private val viewFactory = FunctionCenterViewFactory(activity, dp)
    private var page: View? = null
    private var backAction: (() -> Unit)? = null

    fun showPage(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        attachPage(viewFactory.createPage(title, onBack, buildContent), onBack)
    }

    fun handleBack(): Boolean {
        if (page == null) {
            return false
        }
        backAction?.invoke() ?: close()
        return true
    }

    fun close(): Boolean {
        val currentPage = page ?: return false
        (currentPage.parent as? ViewGroup)?.removeView(currentPage)
        page = null
        backAction = null
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

    private fun attachPage(page: View, onBack: () -> Unit) {
        val container = rootView as? ViewGroup ?: return
        this.page?.let { currentPage ->
            (currentPage.parent as? ViewGroup)?.removeView(currentPage)
        }

        this.page = page
        backAction = onBack
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
