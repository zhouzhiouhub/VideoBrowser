package com.example.videobrowser.functioncenter

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout

internal data class FunctionCenterPageState(
    val view: View,
    val backAction: (() -> Unit)?
)

internal class FunctionCenterPageContainerController(
    private val rootView: View
) {
    private var page: View? = null
    private var backAction: (() -> Unit)? = null

    val hasPage: Boolean
        get() = page != null

    fun attach(
        nextPage: View,
        onBack: () -> Unit,
        saveCurrentPage: Boolean,
        onSaveCurrentPage: (FunctionCenterPageState) -> Unit
    ): Boolean {
        val container = rootView as? ViewGroup ?: return false
        page?.let { currentPage ->
            removeFromParent(currentPage)
            if (saveCurrentPage) {
                onSaveCurrentPage(FunctionCenterPageState(currentPage, backAction))
            }
        }

        page = nextPage
        backAction = onBack
        addToContainer(container, nextPage)
        return true
    }

    fun restore(pageState: FunctionCenterPageState): Boolean {
        val container = rootView as? ViewGroup ?: return false
        page?.let(::removeFromParent)
        page = pageState.view
        backAction = pageState.backAction
        addToContainer(container, pageState.view)
        return true
    }

    fun close(): Boolean {
        val currentPage = page ?: return false
        removeFromParent(currentPage)
        page = null
        backAction = null
        return true
    }

    fun invokeBackAction(): Boolean {
        val action = backAction ?: return false
        action()
        return true
    }

    private fun removeFromParent(targetPage: View) {
        (targetPage.parent as? ViewGroup)?.removeView(targetPage)
    }

    private fun addToContainer(container: ViewGroup, targetPage: View) {
        container.addView(
            targetPage,
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
        targetPage.bringToFront()
        targetPage.requestFocus()
    }
}
