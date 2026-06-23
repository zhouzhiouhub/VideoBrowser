package com.example.videobrowser.utils

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R

object PageUrlActions {
    fun copyPageUrl(activity: AppCompatActivity, url: String) {
        ClipboardTextActions.copyPlainText(
            activity = activity,
            labelResId = R.string.clipboard_page_url,
            text = url,
            toastResId = R.string.toast_link_copied
        )
    }

    fun sharePageUrl(activity: AppCompatActivity, url: String) {
        shareTextUrl(activity, url, R.string.action_share_page)
    }

    fun shareLinkUrl(activity: AppCompatActivity, url: String) {
        shareTextUrl(activity, url, R.string.action_share_link)
    }

    fun shareImageLinkUrl(activity: AppCompatActivity, url: String) {
        shareTextUrl(activity, url, R.string.action_share_image_link)
    }

    private fun shareTextUrl(activity: AppCompatActivity, url: String, chooserTitleRes: Int) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        ChooserIntentLauncher.start(
            activity = activity,
            intent = intent,
            chooserTitleRes = chooserTitleRes
        )
    }
}
