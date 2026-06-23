package com.example.videobrowser.download

import android.app.DownloadManager
import android.content.Context

class AndroidSystemDownloadRemover(
    context: Context
) : SystemDownloadRemover {
    private val appContext: Context = context.applicationContext ?: context

    override fun remove(downloadIds: LongArray): Int {
        val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.remove(*downloadIds)
    }
}
