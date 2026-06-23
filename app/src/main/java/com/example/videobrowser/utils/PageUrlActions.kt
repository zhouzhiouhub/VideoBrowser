package com.example.videobrowser.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R

object PageUrlActions {
    fun copyPageUrl(activity: AppCompatActivity, url: String) {
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(activity.getString(R.string.clipboard_page_url), url)
        )
        Toast.makeText(activity, R.string.toast_link_copied, Toast.LENGTH_SHORT).show()
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
        activity.startActivity(Intent.createChooser(intent, activity.getString(chooserTitleRes)))
    }
}
