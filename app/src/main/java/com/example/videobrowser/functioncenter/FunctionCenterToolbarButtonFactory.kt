package com.example.videobrowser.functioncenter

import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.videobrowser.R

internal class FunctionCenterToolbarButtonFactory(
    private val activity: AppCompatActivity,
    private val dp: (Int) -> Int
) {
    fun createButton(
        @DrawableRes iconRes: Int,
        @StringRes labelRes: Int,
        onClick: () -> Unit
    ): ImageButton {
        val label = activity.getString(labelRes)
        return ImageButton(activity).apply {
            setImageResource(iconRes)
            setColorFilter(ContextCompat.getColor(activity, R.color.browser_icon))
            background = ContextCompat.getDrawable(activity, R.drawable.bg_icon_button)
            contentDescription = label
            scaleType = ImageView.ScaleType.CENTER
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setOnClickListener { onClick() }
        }.also { button ->
            ViewCompat.setTooltipText(button, label)
        }
    }

    fun layoutParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(dp(52), ViewGroup.LayoutParams.MATCH_PARENT)
    }
}
