package com.example.videobrowser.functioncenter

import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

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

    fun showBottomSheetPage(
        title: String,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.showBottomSheetPage(title, onClose, buildContent)
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
