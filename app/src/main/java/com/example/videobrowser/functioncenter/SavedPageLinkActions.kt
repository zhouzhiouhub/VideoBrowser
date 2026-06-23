package com.example.videobrowser.functioncenter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.utils.PageUrlActions

class SavedPageLinkActions(
    private val activity: AppCompatActivity
) {
    fun copyUrl(page: SavedPage) {
        PageUrlActions.copyPageUrl(activity, page.url)
    }

    fun shareUrl(page: SavedPage) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, page.url)
        }
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.action_share_page)))
    }
}
