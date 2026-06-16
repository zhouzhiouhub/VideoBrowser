package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 FunctionCenterPageHost 可以拆开理解为“Function Center Page Host”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.storage.SavedPage

class FunctionCenterPageHost(
    val activity: AppCompatActivity,
    private val functionCenter: FunctionCenterController
) {
    fun showPage(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.showPage(title, onBack, buildContent)
    }

    fun showPage(
        title: String,
        onBack: () -> Unit,
        replaceCurrent: Boolean,
        buildContent: (LinearLayout) -> Unit
    ) {
        if (replaceCurrent) {
            functionCenter.replacePage(title, onBack, buildContent)
        } else {
            functionCenter.showPage(title, onBack, buildContent)
        }
    }

    fun replacePage(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.replacePage(title, onBack, buildContent)
    }

    fun showBottomSheetPage(
        title: String,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.showBottomSheetPage(title, onClose, buildContent)
    }

    fun showBottomSheetPage(
        title: String,
        onBack: () -> Unit,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.showBottomSheetPage(title, onBack, onClose, buildContent)
    }

    fun showBottomSheetPage(
        title: String,
        onBack: () -> Unit,
        onClose: () -> Unit,
        replaceCurrent: Boolean,
        buildContent: (LinearLayout) -> Unit
    ) {
        if (replaceCurrent) {
            functionCenter.replaceBottomSheetPage(title, onBack, onClose, buildContent)
        } else {
            functionCenter.showBottomSheetPage(title, onBack, onClose, buildContent)
        }
    }

    fun replaceBottomSheetPage(
        title: String,
        onBack: () -> Unit,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.replaceBottomSheetPage(title, onBack, onClose, buildContent)
    }

    fun handleBack(): Boolean {
        return functionCenter.handleBack()
    }

    fun close(): Boolean {
        return functionCenter.close()
    }

    fun addFunctionSection(
        parent: LinearLayout,
        title: String,
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.addFunctionSection(parent, title, buildContent)
    }

    fun addInfoRow(parent: LinearLayout, title: String, summary: String) {
        functionCenter.addInfoRow(parent, title, summary)
    }

    fun addFunctionMessage(parent: LinearLayout, message: String) {
        functionCenter.addFunctionMessage(parent, message)
    }

    fun addProfileHeader(parent: LinearLayout, title: String, summary: String, onClick: () -> Unit) {
        functionCenter.addProfileHeader(parent, title, summary, onClick)
    }

    fun addBenefitStrip(
        parent: LinearLayout,
        leftTitle: String,
        leftSummary: String,
        rightTitle: String,
        rightSummary: String
    ) {
        functionCenter.addBenefitStrip(parent, leftTitle, leftSummary, rightTitle, rightSummary)
    }

    fun addHistoryPreview(
        parent: LinearLayout,
        title: String,
        emptyMessage: String,
        pages: List<SavedPage>,
        onOpenPage: (SavedPage) -> Unit,
        onShowHistory: () -> Unit
    ) {
        functionCenter.addHistoryPreview(parent, title, emptyMessage, pages, onOpenPage, onShowHistory)
    }

    fun addEmptyState(parent: LinearLayout, message: String) {
        functionCenter.addEmptyState(parent, message)
    }

    fun addFunctionActionButton(
        parent: LinearLayout,
        title: String,
        backgroundColor: Int? = null,
        onClick: () -> Unit
    ) {
        functionCenter.addFunctionActionButton(parent, title, backgroundColor, onClick)
    }

    fun addActionGrid(
        parent: LinearLayout,
        actions: List<FunctionCenterGridAction>
    ) {
        functionCenter.addActionGrid(parent, actions)
    }

    fun addSwitchRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        checked: Boolean,
        enabled: Boolean = true,
        onChanged: (Boolean) -> Unit
    ) {
        functionCenter.addSwitchRow(parent, title, summary, checked, enabled, onChanged)
    }

    fun addActionRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        enabled: Boolean = true,
        onClick: () -> Unit
    ) {
        functionCenter.addActionRow(parent, title, summary, enabled, onClick)
    }

    fun addDivider(parent: LinearLayout) {
        functionCenter.addDivider(parent)
    }
}
