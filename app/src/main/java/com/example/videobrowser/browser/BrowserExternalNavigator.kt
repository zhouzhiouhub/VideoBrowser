package com.example.videobrowser.browser

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.video.ExternalSubtitleCandidate
import com.example.videobrowser.video.PlaybackQueue
import com.example.videobrowser.video.PlayerActivity

class BrowserExternalNavigator(
    private val activity: AppCompatActivity,
    private val browserManager: () -> BrowserManager,
    private val currentPageTitle: () -> String,
    private val currentShareableUrl: () -> String?,
    private val isShareableUrl: (String) -> Boolean
) {
    fun openExternalUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            activity.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
        }
    }

    fun openNativePlayer(
        url: String,
        mimeType: String? = null,
        userAgentOverride: String? = null,
        titleOverride: String? = null,
        privateBrowsing: Boolean = false,
        subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList(),
        playbackQueue: PlaybackQueue? = null
    ) {
        val title = titleOverride
            ?.takeIf { it.isNotBlank() }
            ?: currentPageTitle()
                .takeIf { it.isNotBlank() && !it.equals(url, ignoreCase = true) }
            ?: URLUtil.guessFileName(url, null, mimeType)
        val isRemoteMedia = isShareableUrl(url)
        val referer = if (isRemoteMedia) {
            currentShareableUrl()?.takeIf { !it.equals(url, ignoreCase = true) }
        } else {
            null
        }
        val cookie = if (isRemoteMedia) {
            CookieManager.getInstance().getCookie(url)
        } else {
            null
        }
        val intent = PlayerActivity.createIntent(
            context = activity,
            mediaUri = url,
            title = title,
            mimeType = mimeType,
            userAgent = userAgentOverride ?: browserManager().userAgentString(),
            cookie = cookie,
            referer = referer,
            privateBrowsing = privateBrowsing,
            subtitleCandidates = subtitleCandidates,
            playbackQueue = playbackQueue
        )
        activity.startActivity(intent)
    }
}
