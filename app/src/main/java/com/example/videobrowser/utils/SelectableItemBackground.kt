package com.example.videobrowser.utils

import android.util.TypedValue
import android.view.View

internal fun View.setBoundedSelectableItemBackground() {
    val outValue = TypedValue()
    context.theme.resolveAttribute(
        android.R.attr.selectableItemBackground,
        outValue,
        true
    )
    setBackgroundResource(outValue.resourceId)
}
