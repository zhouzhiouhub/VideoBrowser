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
        if (!ExternalProtocolPolicy.isWebUrl(url)) {
            Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        try {
            activity.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
        }
    }

    fun openExternalProtocolUrl(
        url: String,
        loadFallbackUrl: (String) -> Unit = {}
    ): Boolean {
        val uri = Uri.parse(url)
        val scheme = uri.scheme
        if (!ExternalProtocolPolicy.shouldOpenExternally(scheme)) {
            return false
        }

        if (scheme.equals("intent", ignoreCase = true)) {
            return openIntentUri(url, loadFallbackUrl)
        }

        return startExternalIntent(
            Intent(Intent.ACTION_VIEW, uri).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
        )
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

    private fun openIntentUri(
        url: String,
        loadFallbackUrl: (String) -> Unit
    ): Boolean {
        val parsedIntent = runCatching {
            Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
        }.getOrNull()

        if (parsedIntent == null) {
            Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
            return true
        }

        val fallbackUrl = parsedIntent
            .getStringExtra(ExternalProtocolPolicy.BROWSER_FALLBACK_URL)
            ?.takeIf(ExternalProtocolPolicy::isWebUrl)
            ?: ExternalProtocolPolicy.fallbackUrlFromIntentUri(url)

        parsedIntent.removeExtra(ExternalProtocolPolicy.BROWSER_FALLBACK_URL)
        parsedIntent.addCategory(Intent.CATEGORY_BROWSABLE)
        parsedIntent.component = null
        parsedIntent.setSelector(null)

        if (startExternalIntent(parsedIntent, showFailureToast = false)) {
            return true
        }

        if (fallbackUrl != null) {
            loadFallbackUrl(fallbackUrl)
            return true
        }

        Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
        return true
    }

    private fun startExternalIntent(
        intent: Intent,
        showFailureToast: Boolean = true
    ): Boolean {
        return try {
            activity.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            if (showFailureToast) {
                Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
            }
            false
        } catch (_: SecurityException) {
            if (showFailureToast) {
                Toast.makeText(activity, R.string.toast_no_external_browser, Toast.LENGTH_SHORT).show()
            }
            false
        }
    }
}
