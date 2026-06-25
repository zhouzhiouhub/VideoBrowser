package com.example.videobrowser.browser.search

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference

internal class QuickLinkLogoBadgeRenderer(
    private val activity: AppCompatActivity,
    private val dp: (Int) -> Int
) {
    fun renderShortcutLogo(badge: TextView, url: String) {
        val cacheKey = QuickLinkLogoResolver.cacheKey(url) ?: return
        val renderKey = "$TAG_PREFIX$cacheKey"
        badge.tag = renderKey
        val badgeReference = WeakReference(badge)
        QuickLinkLogoLoader.load(url) { bitmap ->
            val currentBadge = badgeReference.get() ?: return@load
            if (currentBadge.tag != renderKey || bitmap == null) {
                return@load
            }
            currentBadge.showLogo(bitmap)
        }
    }

    private fun TextView.showLogo(bitmap: Bitmap) {
        val iconSize = dp(24)
        val scaledBitmap = if (bitmap.width == iconSize && bitmap.height == iconSize) {
            bitmap
        } else {
            Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, true)
        }
        val drawable = BitmapDrawable(activity.resources, scaledBitmap).apply {
            setBounds(0, 0, iconSize, iconSize)
        }
        text = ""
        setCompoundDrawables(null, drawable, null, null)
    }

    private companion object {
        private const val TAG_PREFIX = "quick_link_logo:"
    }
}
