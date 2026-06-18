package com.example.videobrowser.functioncenter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage

class SavedPageLinkActions(
    private val activity: AppCompatActivity
) {
    fun copyUrl(page: SavedPage) {
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText(
                activity.getString(R.string.clipboard_page_url),
                page.url
            )
        )
        Toast.makeText(activity, R.string.toast_link_copied, Toast.LENGTH_SHORT).show()
    }

    fun shareUrl(page: SavedPage) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, page.url)
        }
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.action_share_page)))
    }
}
