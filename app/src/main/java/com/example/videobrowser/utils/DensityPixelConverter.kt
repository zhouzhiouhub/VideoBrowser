package com.example.videobrowser.utils

import android.content.res.Resources
import kotlin.math.roundToInt

object DensityPixelConverter {
    fun truncateDp(value: Int, resources: Resources): Int {
        return truncateDp(value, resources.displayMetrics.density)
    }

    fun truncateDp(value: Int, density: Float): Int {
        return (value * density).toInt()
    }

    fun roundDp(value: Int, resources: Resources): Int {
        return roundDp(value, resources.displayMetrics.density)
    }

    fun roundDp(value: Int, density: Float): Int {
        return (value * density).roundToInt()
    }
}
