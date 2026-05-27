package com.example.videobrowser.video

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.example.videobrowser.R

class PlayerActivity : AppCompatActivity() {
    private lateinit var playerRoot: FrameLayout
    private lateinit var playerView: PlayerView
    private lateinit var gestureOverlay: FullscreenVideoGestureOverlay
    private var player: ExoPlayer? = null
    private var playbackPosition = 0L
    private var playWhenReady = true
    private var currentMediaItemIndex = 0
    private var selectedPlaybackSpeed = DEFAULT_PLAYBACK_SPEED
    private var isLandscape = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        volumeControlStream = AudioManager.STREAM_MUSIC
        setContentView(R.layout.activity_player)

        playerRoot = findViewById(R.id.playerRoot)
        playerView = findViewById(R.id.playerView)
        playerView.keepScreenOn = true

        savedInstanceState?.let {
            playbackPosition = it.getLong(STATE_PLAYBACK_POSITION)
            playWhenReady = it.getBoolean(STATE_PLAY_WHEN_READY, true)
            currentMediaItemIndex = it.getInt(STATE_MEDIA_ITEM_INDEX)
            selectedPlaybackSpeed = it.getFloat(STATE_PLAYBACK_SPEED, DEFAULT_PLAYBACK_SPEED)
            isLandscape = it.getBoolean(STATE_LANDSCAPE, true)
        } ?: run {
            selectedPlaybackSpeed = initialPlaybackSpeed()
        }
        applyRequestedOrientation()
        setupGestureOverlay()

        if (mediaUri().isBlank()) {
            Toast.makeText(this, R.string.toast_media_url_invalid, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        title = intent.getStringExtra(EXTRA_MEDIA_TITLE).orEmpty()
        hideSystemBars()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (::gestureOverlay.isInitialized) {
            gestureOverlay.showOverlay()
        }
        hideSystemBars()
    }

    override fun onPause() {
        if (::gestureOverlay.isInitialized) {
            gestureOverlay.hideOverlay()
        }
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        savePlayerState()
        outState.putLong(STATE_PLAYBACK_POSITION, playbackPosition)
        outState.putBoolean(STATE_PLAY_WHEN_READY, playWhenReady)
        outState.putInt(STATE_MEDIA_ITEM_INDEX, currentMediaItemIndex)
        outState.putFloat(STATE_PLAYBACK_SPEED, selectedPlaybackSpeed)
        outState.putBoolean(STATE_LANDSCAPE, isLandscape)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        releasePlayer()
        super.onStop()
    }

    private fun initializePlayer() {
        if (player != null) {
            return
        }

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(requestHeaders())
        intent.getStringExtra(EXTRA_USER_AGENT)
            ?.takeIf { it.isNotBlank() }
            ?.let { dataSourceFactory.setUserAgent(it) }

        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(mediaUri()))
            .setMimeType(normalizedMimeType(intent.getStringExtra(EXTRA_MIME_TYPE)))
            .build()

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .also { exoPlayer ->
                exoPlayer.addListener(
                    object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            Toast.makeText(
                                this@PlayerActivity,
                                R.string.toast_media_playback_failed,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
                playerView.player = exoPlayer
                exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.setPlaybackSpeed(selectedPlaybackSpeed)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentMediaItemIndex, playbackPosition)
                exoPlayer.prepare()
            }
    }

    private fun setupGestureOverlay() {
        gestureOverlay = FullscreenVideoGestureOverlay(this).apply {
            onSeekBy = ::seekPlayerBy
            onPlaybackSpeedSelected = ::setPlayerPlaybackSpeed
            onToggleOrientation = ::togglePlayerOrientation
            setPlaybackSpeed(selectedPlaybackSpeed)
            setLandscape(isLandscape)
        }
        playerRoot.addView(
            gestureOverlay,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun seekPlayerBy(offsetMs: Long) {
        val exoPlayer = player ?: return
        val target = exoPlayer.currentPosition + offsetMs
        val duration = exoPlayer.duration
        val boundedTarget = if (duration != C.TIME_UNSET && duration > 0) {
            target.coerceIn(0L, duration)
        } else {
            target.coerceAtLeast(0L)
        }
        exoPlayer.seekTo(boundedTarget)
    }

    private fun setPlayerPlaybackSpeed(speed: Float) {
        selectedPlaybackSpeed = normalizePlaybackSpeed(speed)
        player?.setPlaybackSpeed(selectedPlaybackSpeed)
    }

    private fun togglePlayerOrientation(): Boolean {
        isLandscape = !isLandscape
        applyRequestedOrientation()
        return isLandscape
    }

    private fun applyRequestedOrientation() {
        requestedOrientation = if (isLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
        if (::gestureOverlay.isInitialized) {
            gestureOverlay.setLandscape(isLandscape)
        }
    }

    private fun releasePlayer() {
        savePlayerState()
        playerView.player = null
        player?.release()
        player = null
    }

    private fun savePlayerState() {
        player?.let {
            playbackPosition = it.currentPosition
            playWhenReady = it.playWhenReady
            currentMediaItemIndex = it.currentMediaItemIndex
        }
    }

    private fun requestHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        intent.getStringExtra(EXTRA_COOKIE)
            ?.takeIf { it.isNotBlank() }
            ?.let { headers["Cookie"] = it }
        intent.getStringExtra(EXTRA_REFERER)
            ?.takeIf { it.isNotBlank() }
            ?.let { headers["Referer"] = it }
        return headers
    }

    private fun normalizedMimeType(mimeType: String?): String? {
        return when (mimeType?.substringBefore(';')?.trim()?.lowercase()) {
            "application/vnd.apple.mpegurl",
            "application/x-mpegurl",
            "audio/mpegurl",
            "audio/x-mpegurl" -> MIME_HLS
            "application/dash+xml" -> MIME_DASH
            "application/vnd.ms-sstr+xml" -> MIME_SMOOTH_STREAMING
            null,
            "" -> null
            else -> mimeType.substringBefore(';').trim()
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun mediaUri(): String {
        return intent.getStringExtra(EXTRA_MEDIA_URI).orEmpty()
    }

    private fun initialPlaybackSpeed(): Float {
        val speed = intent.getFloatExtra(EXTRA_PLAYBACK_SPEED, DEFAULT_PLAYBACK_SPEED)
        return normalizePlaybackSpeed(speed)
    }

    private fun normalizePlaybackSpeed(speed: Float): Float {
        return if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) {
            speed
        } else {
            DEFAULT_PLAYBACK_SPEED
        }
    }

    companion object {
        private const val EXTRA_MEDIA_URI = "com.example.videobrowser.extra.MEDIA_URI"
        private const val EXTRA_MEDIA_TITLE = "com.example.videobrowser.extra.MEDIA_TITLE"
        private const val EXTRA_MIME_TYPE = "com.example.videobrowser.extra.MIME_TYPE"
        private const val EXTRA_USER_AGENT = "com.example.videobrowser.extra.USER_AGENT"
        private const val EXTRA_COOKIE = "com.example.videobrowser.extra.COOKIE"
        private const val EXTRA_REFERER = "com.example.videobrowser.extra.REFERER"
        private const val EXTRA_PLAYBACK_SPEED = "com.example.videobrowser.extra.PLAYBACK_SPEED"
        private const val STATE_PLAYBACK_POSITION = "playback_position"
        private const val STATE_PLAY_WHEN_READY = "play_when_ready"
        private const val STATE_MEDIA_ITEM_INDEX = "media_item_index"
        private const val STATE_PLAYBACK_SPEED = "playback_speed"
        private const val STATE_LANDSCAPE = "landscape"
        private const val DEFAULT_PLAYBACK_SPEED = 1f
        private const val MIME_HLS = "application/x-mpegURL"
        private const val MIME_DASH = "application/dash+xml"
        private const val MIME_SMOOTH_STREAMING = "application/vnd.ms-sstr+xml"

        fun createIntent(
            context: Context,
            mediaUri: String,
            title: String?,
            mimeType: String?,
            userAgent: String?,
            cookie: String?,
            referer: String?,
            playbackSpeed: Float
        ): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_MEDIA_URI, mediaUri)
                putExtra(EXTRA_MEDIA_TITLE, title)
                putExtra(EXTRA_MIME_TYPE, mimeType)
                putExtra(EXTRA_USER_AGENT, userAgent)
                putExtra(EXTRA_COOKIE, cookie)
                putExtra(EXTRA_REFERER, referer)
                putExtra(EXTRA_PLAYBACK_SPEED, playbackSpeed)
            }
        }
    }
}
