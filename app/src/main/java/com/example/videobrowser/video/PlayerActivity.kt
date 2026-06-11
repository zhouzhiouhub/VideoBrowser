package com.example.videobrowser.video

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
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
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.example.videobrowser.R
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.PreferenceStore

class PlayerActivity : AppCompatActivity() {
    private lateinit var playerRoot: FrameLayout
    private lateinit var playerView: PlayerView
    private lateinit var gestureOverlay: FullscreenVideoGestureOverlay
    private lateinit var settingsManager: SettingsManager
    private lateinit var playbackHistoryRepository: PlaybackHistoryRepository
    private var player: ExoPlayer? = null
    private val scanHandler = Handler(Looper.getMainLooper())
    private var playbackPosition = 0L
    private var playWhenReady = true
    private var currentMediaItemIndex = 0
    private var selectedPlaybackSpeed = DEFAULT_PLAYBACK_SPEED
    private var longPressRestoreSpeed = DEFAULT_PLAYBACK_SPEED
    private var longPressRestorePlayWhenReady = true
    private var longPressDirection = 0
    private var directionalLongPressActive = false
    private var isLandscape = true
    private var videoEffectsEnabled = true
    private var retriedPlaybackWithoutVideoEffects = false

    private val reverseScanRunnable = object : Runnable {
        override fun run() {
            if (!directionalLongPressActive || longPressDirection >= 0) {
                return
            }
            seekPlayerBy(-LONG_PRESS_SCAN_STEP_MS)
            scanHandler.postDelayed(this, LONG_PRESS_SCAN_INTERVAL_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        volumeControlStream = AudioManager.STREAM_MUSIC
        setContentView(R.layout.activity_player)
        val preferenceStore = PreferenceStore.from(this)
        settingsManager = SettingsManager(preferenceStore)
        playbackHistoryRepository = PlaybackHistoryRepository(preferenceStore)
        selectedPlaybackSpeed = settingsManager.defaultVideoSpeed()
        longPressRestoreSpeed = selectedPlaybackSpeed

        playerRoot = findViewById(R.id.playerRoot)
        playerView = findViewById(R.id.playerView)
        playerView.keepScreenOn = true
        playerView.setControllerShowTimeoutMs(CONTROLS_HIDE_DELAY_MS)

        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.getLong(STATE_PLAYBACK_POSITION)
            playWhenReady = savedInstanceState.getBoolean(STATE_PLAY_WHEN_READY, true)
            currentMediaItemIndex = savedInstanceState.getInt(STATE_MEDIA_ITEM_INDEX)
            isLandscape = savedInstanceState.getBoolean(STATE_LANDSCAPE, true)
            selectedPlaybackSpeed = normalizePlaybackSpeed(
                savedInstanceState.getFloat(STATE_PLAYBACK_SPEED, selectedPlaybackSpeed)
            )
            longPressRestoreSpeed = selectedPlaybackSpeed
            videoEffectsEnabled = savedInstanceState.getBoolean(STATE_VIDEO_EFFECTS_ENABLED, true)
            retriedPlaybackWithoutVideoEffects = savedInstanceState.getBoolean(
                STATE_RETRIED_WITHOUT_VIDEO_EFFECTS,
                false
            )
        } else {
            restorePlaybackHistory()
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

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            wakePlayerControls()
        }
        return super.dispatchKeyEvent(event)
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
        wakePlayerControls()
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
        outState.putBoolean(STATE_LANDSCAPE, isLandscape)
        outState.putFloat(STATE_PLAYBACK_SPEED, selectedPlaybackSpeed)
        outState.putBoolean(STATE_VIDEO_EFFECTS_ENABLED, videoEffectsEnabled)
        outState.putBoolean(
            STATE_RETRIED_WITHOUT_VIDEO_EFFECTS,
            retriedPlaybackWithoutVideoEffects
        )
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        releasePlayer()
        super.onStop()
    }

    private fun initializePlayer() {
        if (player != null) {
            Log.d(VIDEO_LOG_TAG, "event=native-initialize skipped=true")
            return
        }
        Log.d(
            VIDEO_LOG_TAG,
            "event=native-initialize uri=${mediaUri().take(180)} mime=${intent.getStringExtra(EXTRA_MIME_TYPE)}"
        )

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(requestHeaders())
        intent.getStringExtra(EXTRA_USER_AGENT)
            ?.takeIf { it.isNotBlank() }
            ?.let { dataSourceFactory.setUserAgent(it) }

        val mediaSourceFactory = DefaultMediaSourceFactory(
            DefaultDataSource.Factory(this, dataSourceFactory)
        )
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(mediaUri()))
            .setMimeType(normalizedMimeType(intent.getStringExtra(EXTRA_MIME_TYPE)))
            .build()
        val videoEffects = NativeVideoEnhancement.defaultEffects()

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .also { exoPlayer ->
                if (videoEffectsEnabled && videoEffects.isNotEmpty()) {
                    runCatching {
                        exoPlayer.setVideoEffects(videoEffects)
                    }.onSuccess {
                        Log.d(
                            VIDEO_LOG_TAG,
                            "event=native-video-effects applied=true count=${videoEffects.size}"
                        )
                    }.onFailure { error ->
                        videoEffectsEnabled = false
                        Log.d(
                            VIDEO_LOG_TAG,
                            "event=native-video-effects applied=false error=${error.message}"
                        )
                    }
                }
                exoPlayer.addListener(
                    object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            Log.d(
                                VIDEO_LOG_TAG,
                                "event=native-player-error code=${error.errorCode} message=${error.message}"
                            )
                            if (videoEffectsEnabled && !retriedPlaybackWithoutVideoEffects) {
                                retryPlaybackWithoutVideoEffects()
                                return
                            }
                            Toast.makeText(
                                this@PlayerActivity,
                                R.string.toast_media_playback_failed,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onPlayWhenReadyChanged(
                            playWhenReady: Boolean,
                            reason: Int
                        ) {
                            Log.d(
                                VIDEO_LOG_TAG,
                                "event=native-play-when-ready playWhenReady=$playWhenReady reason=$reason"
                            )
                            wakePlayerControls()
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            Log.d(
                                VIDEO_LOG_TAG,
                                "event=native-playback-state state=$playbackState"
                            )
                            wakePlayerControls()
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

    private fun retryPlaybackWithoutVideoEffects() {
        if (retriedPlaybackWithoutVideoEffects) {
            return
        }
        Log.d(VIDEO_LOG_TAG, "event=native-video-effects retryWithoutEffects=true")
        savePlayerState()
        retriedPlaybackWithoutVideoEffects = true
        videoEffectsEnabled = false
        releasePlayer()
        initializePlayer()
    }

    private fun setupGestureOverlay() {
        gestureOverlay = FullscreenVideoGestureOverlay(this).apply {
            onSeekBy = ::seekPlayerBy
            onSeekTo = ::seekPlayerTo
            onSeekPreviewStart = ::currentPlayerSeekPosition
            onTogglePlayPause = ::togglePlayerPlayPause
            onPlaybackSpeedSelected = ::setPlayerPlaybackSpeed
            onDirectionalLongPressStart = ::startDirectionalLongPress
            onDirectionalLongPressEnd = ::stopDirectionalLongPress
            onToggleOrientation = ::togglePlayerOrientation
            onUserInteraction = ::wakePlayerControls
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
        seekPlayerTo(exoPlayer.currentPosition + offsetMs)
    }

    private fun seekPlayerTo(positionMs: Long) {
        val exoPlayer = player ?: return
        val duration = exoPlayer.duration
        val boundedTarget = if (duration != C.TIME_UNSET && duration > 0) {
            positionMs.coerceIn(0L, duration)
        } else {
            positionMs.coerceAtLeast(0L)
        }
        exoPlayer.seekTo(boundedTarget)
    }

    private fun currentPlayerSeekPosition(): FullscreenVideoGestureOverlay.SeekPosition? {
        val exoPlayer = player ?: return null
        val duration = exoPlayer.duration.takeIf { it != C.TIME_UNSET && it > 0L }
        return FullscreenVideoGestureOverlay.SeekPosition(
            positionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
            durationMs = duration
        )
    }

    private fun togglePlayerPlayPause(): Boolean? {
        val exoPlayer = player ?: return null
        Log.d(
            VIDEO_LOG_TAG,
            "event=native-toggle-playback isPlaying=${exoPlayer.isPlaying} " +
                "playWhenReady=${exoPlayer.playWhenReady} state=${exoPlayer.playbackState}"
        )
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_ENDED) {
                exoPlayer.seekTo(0)
            }
            exoPlayer.play()
        }
        wakePlayerControls()
        return exoPlayer.playWhenReady
    }

    private fun wakePlayerControls() {
        if (!::playerView.isInitialized) {
            return
        }
        val keepVisible = shouldKeepPlayerControlsVisible()
        Log.d(
            VIDEO_LOG_TAG,
            "event=native-wake-controls keepVisible=$keepVisible " +
                "state=${player?.playbackState} playWhenReady=${player?.playWhenReady}"
        )
        playerView.setControllerShowTimeoutMs(
            if (keepVisible) {
                0
            } else {
                CONTROLS_HIDE_DELAY_MS
            }
        )
        playerView.showController()
    }

    private fun shouldKeepPlayerControlsVisible(): Boolean {
        val exoPlayer = player ?: return false
        return !exoPlayer.playWhenReady ||
            exoPlayer.playbackState == Player.STATE_IDLE ||
            exoPlayer.playbackState == Player.STATE_ENDED
    }

    private fun setPlayerPlaybackSpeed(speed: Float) {
        selectedPlaybackSpeed = normalizePlaybackSpeed(speed)
        settingsManager.setDefaultVideoSpeed(selectedPlaybackSpeed)
        player?.setPlaybackSpeed(selectedPlaybackSpeed)
    }

    private fun startDirectionalLongPress(direction: Int) {
        val exoPlayer = player ?: return
        stopDirectionalLongPress()

        directionalLongPressActive = true
        longPressDirection = if (direction < 0) -1 else 1
        longPressRestoreSpeed = selectedPlaybackSpeed
        longPressRestorePlayWhenReady = exoPlayer.playWhenReady

        if (longPressDirection > 0) {
            exoPlayer.setPlaybackSpeed(LONG_PRESS_PLAYBACK_SPEED)
            exoPlayer.play()
        } else {
            exoPlayer.pause()
            seekPlayerBy(-LONG_PRESS_SCAN_STEP_MS)
            scanHandler.postDelayed(reverseScanRunnable, LONG_PRESS_SCAN_INTERVAL_MS)
        }
    }

    private fun stopDirectionalLongPress() {
        if (!directionalLongPressActive) {
            return
        }
        directionalLongPressActive = false
        longPressDirection = 0
        scanHandler.removeCallbacks(reverseScanRunnable)

        val exoPlayer = player ?: return
        selectedPlaybackSpeed = normalizePlaybackSpeed(longPressRestoreSpeed)
        exoPlayer.setPlaybackSpeed(selectedPlaybackSpeed)
        if (longPressRestorePlayWhenReady) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
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
        stopDirectionalLongPress()
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
            savePlaybackHistory(it)
        }
    }

    private fun restorePlaybackHistory() {
        val progress = playbackHistoryRepository.progressFor(playbackHistoryIdentity())
        val resumePosition = playbackHistoryRepository.resumePositionFor(playbackHistoryIdentity())
        if (resumePosition != null) {
            playbackPosition = resumePosition
        }
        if (progress != null) {
            selectedPlaybackSpeed = normalizePlaybackSpeed(progress.speed)
            longPressRestoreSpeed = selectedPlaybackSpeed
        }
    }

    private fun savePlaybackHistory(exoPlayer: ExoPlayer) {
        val identity = playbackHistoryIdentity()
        if (identity.isBlank()) {
            return
        }
        val duration = exoPlayer.duration.takeIf { it != C.TIME_UNSET && it > 0L } ?: 0L
        playbackHistoryRepository.save(
            PlaybackProgress(
                mediaIdentity = identity,
                positionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                durationMs = duration,
                speed = selectedPlaybackSpeed,
                updatedAtMillis = System.currentTimeMillis()
            ),
            privateBrowsing = isPrivateBrowsingPlayback()
        )
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

    private fun playbackHistoryIdentity(): String {
        return mediaUri().trim()
    }

    private fun isPrivateBrowsingPlayback(): Boolean {
        return intent.getBooleanExtra(EXTRA_PRIVATE_BROWSING, false)
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
        private const val EXTRA_PRIVATE_BROWSING =
            "com.example.videobrowser.extra.PRIVATE_BROWSING"
        private const val STATE_PLAYBACK_POSITION = "playback_position"
        private const val STATE_PLAY_WHEN_READY = "play_when_ready"
        private const val STATE_MEDIA_ITEM_INDEX = "media_item_index"
        private const val STATE_LANDSCAPE = "landscape"
        private const val STATE_PLAYBACK_SPEED = "playback_speed"
        private const val STATE_VIDEO_EFFECTS_ENABLED = "video_effects_enabled"
        private const val STATE_RETRIED_WITHOUT_VIDEO_EFFECTS = "retried_without_video_effects"
        private const val VIDEO_LOG_TAG = "VideoBrowserVideo"
        private const val DEFAULT_PLAYBACK_SPEED = 1f
        private const val CONTROLS_HIDE_DELAY_MS = 3000
        private const val LONG_PRESS_PLAYBACK_SPEED = 2f
        private const val LONG_PRESS_SCAN_STEP_MS = 500L
        private const val LONG_PRESS_SCAN_INTERVAL_MS = 250L
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
            privateBrowsing: Boolean = false
        ): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_MEDIA_URI, mediaUri)
                putExtra(EXTRA_MEDIA_TITLE, title)
                putExtra(EXTRA_MIME_TYPE, mimeType)
                putExtra(EXTRA_USER_AGENT, userAgent)
                putExtra(EXTRA_COOKIE, cookie)
                putExtra(EXTRA_REFERER, referer)
                putExtra(EXTRA_PRIVATE_BROWSING, privateBrowsing)
            }
        }
    }
}
