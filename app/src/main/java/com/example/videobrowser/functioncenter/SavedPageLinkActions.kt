package com.example.videobrowser.functioncenter

import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.storage.SavedPage
import com.example.videobrowser.utils.PageUrlActions

class SavedPageLinkActions(
    private val activity: AppCompatActivity
) {
    fun copyUrl(page: SavedPage) {
        PageUrlActions.copyPageUrl(activity, page.url)
    }

    fun shareUrl(page: SavedPage) {
        PageUrlActions.sharePageUrl(activity, page.url)
    }
}
