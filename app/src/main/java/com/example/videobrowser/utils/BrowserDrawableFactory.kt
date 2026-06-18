package com.example.videobrowser.utils

import android.graphics.drawable.GradientDrawable

internal object BrowserDrawableFactory {
    fun roundedBackground(
        color: Int,
        radius: Int,
        strokeWidth: Int = 0,
        strokeColor: Int? = null
    ): GradientDrawable {
        return roundedBackground(color, radius.toFloat(), strokeWidth, strokeColor)
    }

    fun roundedBackground(
        color: Int,
        radius: Float,
        strokeWidth: Int = 0,
        strokeColor: Int? = null
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            applyStroke(strokeWidth, strokeColor)
        }
    }

    fun topRoundedBackground(color: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            setCornerRadii(
                floatArrayOf(
                    radius,
                    radius,
                    radius,
                    radius,
                    0f,
                    0f,
                    0f,
                    0f
                )
            )
        }
    }

    fun circleBackground(
        color: Int,
        strokeWidth: Int = 0,
        strokeColor: Int? = null
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            applyStroke(strokeWidth, strokeColor)
        }
    }

    private fun GradientDrawable.applyStroke(strokeWidth: Int, strokeColor: Int?) {
        if (strokeWidth > 0 && strokeColor != null) {
            setStroke(strokeWidth, strokeColor)
        }
    }
}
