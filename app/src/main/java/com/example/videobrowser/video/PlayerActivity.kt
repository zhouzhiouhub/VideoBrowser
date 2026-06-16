package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 PlayerActivity 可以拆开理解为“Player Activity”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
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
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.TrackSelectionDialogBuilder
import androidx.media3.ui.PlayerView
import com.example.videobrowser.R
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.PreferenceStore
import org.json.JSONArray
import org.json.JSONObject

/**
 * 原生视频播放器界面。
 *
 * 它使用 AndroidX Media3 ExoPlayer 播放直接视频地址或本地视频文件。
 * MainActivity 负责发现“应该原生播放”的媒体，真正的播放、手势、字幕、队列和历史都在这里处理。
 */
class PlayerActivity : AppCompatActivity() {
    private lateinit var playerRoot: FrameLayout
    private lateinit var playerView: PlayerView
    private lateinit var gestureOverlay: FullscreenVideoGestureOverlay
    private lateinit var settingsManager: SettingsManager
    private lateinit var playbackHistoryRepository: PlaybackHistoryRepository
    private lateinit var playbackQueue: PlaybackQueue
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
    private var repeatMode = PlaybackRepeatMode.NONE
    private var videoZoomMode = VideoZoomMode.FIT

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
        // 播放器默认横屏，并把系统音量键绑定到媒体音量。
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        volumeControlStream = AudioManager.STREAM_MUSIC
        setContentView(R.layout.activity_player)
        val preferenceStore = PreferenceStore.from(this)
        settingsManager = SettingsManager(preferenceStore)
        playbackHistoryRepository = PlaybackHistoryRepository(preferenceStore)
        val savedPlaybackQueue = if (savedInstanceState != null) {
            savedInstanceState.getString(STATE_PLAYBACK_QUEUE)?.let(::decodePlaybackQueue)
        } else {
            null
        }
        // 横竖屏切换或系统重建 Activity 时优先恢复队列；首次进入则从 Intent 解析队列。
        playbackQueue = savedPlaybackQueue ?: playbackQueueFromIntent()
        currentMediaItemIndex = playbackQueue.currentIndex
        repeatMode = playbackQueue.repeatMode
        selectedPlaybackSpeed = settingsManager.defaultVideoSpeed()
        longPressRestoreSpeed = selectedPlaybackSpeed

        playerRoot = findViewById(R.id.playerRoot)
        playerView = findViewById(R.id.playerView)
        playerView.keepScreenOn = true
        playerView.setControllerShowTimeoutMs(CONTROLS_HIDE_DELAY_MS)

        if (savedInstanceState != null) {
            // 系统重建时恢复内存状态，避免旋转或后台回收后从头播放。
            playbackPosition = savedInstanceState.getLong(STATE_PLAYBACK_POSITION)
            playWhenReady = savedInstanceState.getBoolean(STATE_PLAY_WHEN_READY, true)
            currentMediaItemIndex = savedInstanceState.getInt(STATE_MEDIA_ITEM_INDEX)
                .let { index ->
                    if (playbackQueue.items.isEmpty()) {
                        0
                    } else {
                        index.coerceIn(0, playbackQueue.items.lastIndex)
                    }
                }
            playbackQueue = playbackQueue.select(currentMediaItemIndex)
            isLandscape = savedInstanceState.getBoolean(STATE_LANDSCAPE, true)
            selectedPlaybackSpeed = normalizePlaybackSpeed(
                savedInstanceState.getFloat(STATE_PLAYBACK_SPEED, selectedPlaybackSpeed)
            )
            repeatMode = savedInstanceState.getString(STATE_REPEAT_MODE)
                ?.let { runCatching { PlaybackRepeatMode.valueOf(it) }.getOrNull() }
                ?: repeatMode
            playbackQueue = playbackQueue.copy(repeatMode = repeatMode)
            videoZoomMode = savedInstanceState.getString(STATE_VIDEO_ZOOM_MODE)
                ?.let { runCatching { VideoZoomMode.valueOf(it) }.getOrNull() }
                ?: videoZoomMode
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
        applyVideoZoomMode()
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
        val sessionState = currentPlaybackSessionState()
        outState.putLong(STATE_PLAYBACK_POSITION, sessionState.positionMs)
        outState.putBoolean(STATE_PLAY_WHEN_READY, sessionState.playWhenReady)
        outState.putInt(STATE_MEDIA_ITEM_INDEX, sessionState.currentIndex)
        outState.putBoolean(STATE_LANDSCAPE, isLandscape)
        outState.putFloat(STATE_PLAYBACK_SPEED, sessionState.speed)
        outState.putString(STATE_REPEAT_MODE, sessionState.repeatMode.name)
        outState.putString(STATE_PLAYBACK_QUEUE, PlaybackQueueJson.encode(playbackQueue))
        outState.putString(STATE_VIDEO_ZOOM_MODE, sessionState.zoomMode.name)
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
        // WebView 传来的 User-Agent、Cookie、Referer 会放进请求头，减少站点防盗链导致的播放失败。
        intent.getStringExtra(EXTRA_USER_AGENT)
            ?.takeIf { it.isNotBlank() }
            ?.let { dataSourceFactory.setUserAgent(it) }

        val mediaSourceFactory = DefaultMediaSourceFactory(
            DefaultDataSource.Factory(this, dataSourceFactory)
        )
        val mediaItems = playbackQueue.items.map(::toMediaItem)
        val videoEffects = NativeVideoEnhancement.defaultEffects()

        // ExoPlayer 生命周期跟随 Activity：onStart 初始化，onStop 释放，播放位置由 savePlayerState 保存。
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

                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            currentMediaItemIndex = exoPlayer.currentMediaItemIndex
                            playbackQueue = playbackQueue
                                .select(currentMediaItemIndex)
                                .copy(repeatMode = repeatMode)
                            playbackPosition = 0L
                            title = playbackQueue.items.getOrNull(currentMediaItemIndex)?.title.orEmpty()
                            updateQueueControls()
                            wakePlayerControls()
                        }
                    }
                )
                playerView.player = exoPlayer
                exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true)
                exoPlayer.setMediaItems(mediaItems, currentMediaItemIndex, playbackPosition)
                exoPlayer.repeatMode = media3RepeatMode(repeatMode)
                exoPlayer.setPlaybackSpeed(selectedPlaybackSpeed)
                exoPlayer.playWhenReady = playWhenReady
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
        // 手势层不直接操作 ExoPlayer，而是统一发 PlaybackCommand，再由 handlePlaybackCommand 分发。
        gestureOverlay = FullscreenVideoGestureOverlay(this).apply {
            onSeekBy = { offsetMs -> handlePlaybackCommand(PlaybackCommand.SeekBy(offsetMs)) }
            onSeekTo = { positionMs -> handlePlaybackCommand(PlaybackCommand.SeekTo(positionMs)) }
            onSeekPreviewStart = ::currentPlayerSeekPosition
            onTogglePlayPause = {
                handlePlaybackCommand(PlaybackCommand.TogglePlayPause) as? Boolean
            }
            onPlaybackSpeedSelected = { speed ->
                handlePlaybackCommand(PlaybackCommand.SetSpeed(speed))
            }
            onDirectionalLongPressStart = ::startDirectionalLongPress
            onDirectionalLongPressEnd = ::stopDirectionalLongPress
            onToggleOrientation = ::togglePlayerOrientation
            onUserInteraction = ::wakePlayerControls
            onExitFullscreen = ::finish
            onTrackSelectionRequested = {
                handlePlaybackCommand(PlaybackCommand.ShowTrackSelection)
            }
            onPlaybackQueueRequested = { handlePlaybackCommand(PlaybackCommand.ShowQueue) }
            onVideoZoomRequested = {
                handlePlaybackCommand(PlaybackCommand.CycleZoom) as? VideoZoomMode
                    ?: videoZoomMode
            }
            onPreviousMediaRequested = { handlePlaybackCommand(PlaybackCommand.Previous) }
            onNextMediaRequested = { handlePlaybackCommand(PlaybackCommand.Next) }
            onRepeatModeRequested = {
                handlePlaybackCommand(PlaybackCommand.ToggleRepeat) as? PlaybackRepeatMode
                    ?: repeatMode
            }
            setPlaybackSpeed(selectedPlaybackSpeed)
            setLandscape(isLandscape)
            setQueueControlsVisible(playbackQueue.items.size > 1)
            setRepeatMode(repeatMode)
            setVideoZoomMode(videoZoomMode)
        }
        playerRoot.addView(
            gestureOverlay,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun handlePlaybackCommand(command: PlaybackCommand): Any? {
        // 这一层把 UI 手势/按钮命令转换成播放器动作，后续新增控制项时也从这里接入。
        return when (command) {
            PlaybackCommand.Play -> playPlayer()
            PlaybackCommand.Pause -> pausePlayer()
            PlaybackCommand.TogglePlayPause -> togglePlayerPlayPause()
            is PlaybackCommand.SeekBy -> {
                seekPlayerBy(command.offsetMs)
                Unit
            }
            is PlaybackCommand.SeekTo -> {
                seekPlayerTo(command.positionMs)
                Unit
            }
            is PlaybackCommand.SetSpeed -> {
                setPlayerPlaybackSpeed(command.speed)
                Unit
            }
            PlaybackCommand.Previous -> {
                playPreviousMedia()
                Unit
            }
            PlaybackCommand.Next -> {
                playNextMedia()
                Unit
            }
            PlaybackCommand.ToggleRepeat -> cycleRepeatMode()
            is PlaybackCommand.SelectQueueItem -> {
                playMediaAt(command.index)
                Unit
            }
            PlaybackCommand.ShowQueue -> {
                showPlaybackQueueMenu()
                Unit
            }
            PlaybackCommand.ToggleShuffle -> toggleShuffleMode()
            PlaybackCommand.CycleZoom -> cycleVideoZoomMode()
            PlaybackCommand.ShowTrackSelection -> {
                showTrackSelectionMenu()
                Unit
            }
            is PlaybackCommand.SelectTrack -> {
                when (command.trackType) {
                    PlaybackTrackType.AUDIO -> showTrackSelectionDialog(
                        C.TRACK_TYPE_AUDIO,
                        R.string.video_track_audio
                    )
                    PlaybackTrackType.SUBTITLE -> showTrackSelectionDialog(
                        C.TRACK_TYPE_TEXT,
                        R.string.video_track_subtitles
                    )
                }
                Unit
            }
        }
    }

    private fun currentPlaybackSessionState(): PlaybackSessionState {
        val exoPlayer = player
        val durationMs = exoPlayer
            ?.duration
            ?.takeIf { it != C.TIME_UNSET && it > 0L }
        return PlaybackSessionState.fromQueue(
            queue = playbackQueue,
            positionMs = exoPlayer?.currentPosition ?: playbackPosition,
            durationMs = durationMs,
            speed = selectedPlaybackSpeed,
            playWhenReady = exoPlayer?.playWhenReady ?: playWhenReady,
            zoomMode = videoZoomMode
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

    private fun playPlayer(): Boolean? {
        val exoPlayer = player ?: return null
        if (exoPlayer.playbackState == Player.STATE_ENDED) {
            exoPlayer.seekTo(0)
        }
        exoPlayer.play()
        wakePlayerControls()
        return exoPlayer.playWhenReady
    }

    private fun pausePlayer(): Boolean? {
        val exoPlayer = player ?: return null
        exoPlayer.pause()
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

    private fun playPreviousMedia() {
        val previousIndex = when {
            currentMediaItemIndex > 0 -> currentMediaItemIndex - 1
            repeatMode == PlaybackRepeatMode.ALL && playbackQueue.items.size > 1 -> {
                playbackQueue.items.lastIndex
            }
            else -> currentMediaItemIndex
        }
        playMediaAt(previousIndex)
    }

    private fun playNextMedia() {
        val nextIndex = when {
            currentMediaItemIndex + 1 < playbackQueue.items.size -> currentMediaItemIndex + 1
            repeatMode == PlaybackRepeatMode.ALL && playbackQueue.items.size > 1 -> 0
            else -> currentMediaItemIndex
        }
        playMediaAt(nextIndex)
    }

    private fun playMediaAt(index: Int) {
        val exoPlayer = player ?: return
        if (index !in playbackQueue.items.indices) {
            wakePlayerControls()
            return
        }
        if (index == currentMediaItemIndex) {
            playbackQueue = playbackQueue.select(index)
            wakePlayerControls()
            return
        }

        savePlayerState()
        playbackQueue = playbackQueue.select(index)
        currentMediaItemIndex = playbackQueue.currentIndex
        playbackPosition = playbackHistoryRepository.resumePositionFor(playbackHistoryIdentity()) ?: 0L
        exoPlayer.seekTo(index, playbackPosition)
        exoPlayer.play()
        title = playbackQueue.items[index].title.orEmpty()
        updateQueueControls()
        wakePlayerControls()
    }

    private fun cycleRepeatMode(): PlaybackRepeatMode {
        repeatMode = when (repeatMode) {
            PlaybackRepeatMode.NONE -> PlaybackRepeatMode.ONE
            PlaybackRepeatMode.ONE -> PlaybackRepeatMode.ALL
            PlaybackRepeatMode.ALL -> PlaybackRepeatMode.NONE
        }
        playbackQueue = playbackQueue.copy(repeatMode = repeatMode)
        player?.repeatMode = media3RepeatMode(repeatMode)
        updateQueueControls()
        return repeatMode
    }

    private fun updateQueueControls() {
        if (!::gestureOverlay.isInitialized) {
            return
        }
        gestureOverlay.setQueueControlsVisible(playbackQueue.items.size > 1)
        gestureOverlay.setRepeatMode(repeatMode)
    }

    private fun media3RepeatMode(mode: PlaybackRepeatMode): Int {
        return when (mode) {
            PlaybackRepeatMode.NONE -> Player.REPEAT_MODE_OFF
            PlaybackRepeatMode.ONE -> Player.REPEAT_MODE_ONE
            PlaybackRepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    private fun cycleVideoZoomMode(): VideoZoomMode {
        videoZoomMode = videoZoomMode.next()
        applyVideoZoomMode()
        wakePlayerControls()
        return videoZoomMode
    }

    private fun applyVideoZoomMode() {
        if (::playerView.isInitialized) {
            playerView.resizeMode = videoZoomMode.resizeMode
        }
        if (::gestureOverlay.isInitialized) {
            gestureOverlay.setVideoZoomMode(videoZoomMode)
        }
    }

    private fun toggleShuffleMode(): Boolean {
        if (playbackQueue.items.size <= 1) {
            wakePlayerControls()
            return playbackQueue.isShuffled
        }
        savePlayerState()
        playbackQueue = if (playbackQueue.isShuffled) {
            playbackQueue.restoreOriginalOrder()
        } else {
            playbackQueue.shuffle()
        }
        currentMediaItemIndex = playbackQueue.currentIndex
        syncPlayerQueueToPlaybackQueue()
        updateQueueControls()
        wakePlayerControls()
        return playbackQueue.isShuffled
    }

    private fun syncPlayerQueueToPlaybackQueue() {
        val exoPlayer = player ?: return
        exoPlayer.setMediaItems(playbackQueue.items.map(::toMediaItem), currentMediaItemIndex, playbackPosition)
        exoPlayer.repeatMode = media3RepeatMode(repeatMode)
        exoPlayer.setPlaybackSpeed(selectedPlaybackSpeed)
        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.prepare()
    }

    private fun showPlaybackQueueMenu() {
        if (playbackQueue.items.size <= 1) {
            wakePlayerControls()
            return
        }
        wakePlayerControls()
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.video_queue_title, playbackQueue.items.size))
            .setItems(playbackQueueLabels()) { _, index ->
                handlePlaybackCommand(PlaybackCommand.SelectQueueItem(index))
            }
            .setPositiveButton(shuffleActionLabel()) { _, _ ->
                handlePlaybackCommand(PlaybackCommand.ToggleShuffle)
                showPlaybackQueueMenu()
            }
            .setNeutralButton(R.string.video_queue_remove) { _, _ ->
                showPlaybackQueueRemoveMenu()
            }
            .show()
    }

    private fun shuffleActionLabel(): Int {
        return if (playbackQueue.isShuffled) {
            R.string.video_queue_restore_order
        } else {
            R.string.video_queue_shuffle
        }
    }

    private fun showPlaybackQueueRemoveMenu() {
        if (playbackQueue.items.size <= 1) {
            wakePlayerControls()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.video_queue_remove)
            .setItems(playbackQueueLabels()) { _, index ->
                removeMediaFromQueue(index)
                showPlaybackQueueMenu()
            }
            .show()
    }

    private fun removeMediaFromQueue(index: Int) {
        if (index !in playbackQueue.items.indices || playbackQueue.items.size <= 1) {
            wakePlayerControls()
            return
        }
        savePlayerState()
        val removedCurrentItem = index == currentMediaItemIndex
        playbackQueue = playbackQueue.removeAt(index)
        currentMediaItemIndex = playbackQueue.currentIndex
        val exoPlayer = player
        if (exoPlayer != null && index < exoPlayer.mediaItemCount) {
            exoPlayer.removeMediaItem(index)
            if (removedCurrentItem) {
                playbackPosition = playbackHistoryRepository.resumePositionFor(
                    playbackHistoryIdentity()
                ) ?: 0L
                exoPlayer.seekTo(currentMediaItemIndex, playbackPosition)
                exoPlayer.play()
            }
        }
        title = playbackQueue.currentItem()?.title.orEmpty()
        updateQueueControls()
        wakePlayerControls()
    }

    private fun playbackQueueLabels(): Array<String> {
        return playbackQueue.items.mapIndexed { index, item ->
            val title = item.title
                ?.takeIf { it.isNotBlank() }
                ?: item.uri.substringAfterLast('/').ifBlank { item.uri }
            val currentLabel = if (index == currentMediaItemIndex) {
                " - ${getString(R.string.video_queue_now_playing)}"
            } else {
                ""
            }
            "${index + 1}. $title$currentLabel"
        }.toTypedArray()
    }

    private fun showTrackSelectionMenu() {
        if (player == null) {
            Toast.makeText(this, R.string.toast_video_tracks_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        wakePlayerControls()
        AlertDialog.Builder(this)
            .setTitle(R.string.video_control_tracks)
            .setItems(
                arrayOf(
                    getString(R.string.video_track_audio),
                    getString(R.string.video_track_subtitles)
                )
            ) { _, which ->
                when (which) {
                    0 -> handlePlaybackCommand(PlaybackCommand.SelectTrack(PlaybackTrackType.AUDIO))
                    1 -> handlePlaybackCommand(PlaybackCommand.SelectTrack(PlaybackTrackType.SUBTITLE))
                }
            }
            .show()
    }

    @OptIn(UnstableApi::class)
    private fun showTrackSelectionDialog(trackType: Int, titleResId: Int) {
        val exoPlayer = player ?: run {
            Toast.makeText(this, R.string.toast_video_tracks_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        val hasTracks = exoPlayer.currentTracks.groups.any { group -> group.type == trackType }
        if (!hasTracks) {
            Toast.makeText(this, R.string.toast_video_tracks_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        TrackSelectionDialogBuilder(
            this,
            getString(titleResId),
            exoPlayer,
            trackType
        )
            .setShowDisableOption(trackType == C.TRACK_TYPE_TEXT)
            .setAllowAdaptiveSelections(false)
            .setAllowMultipleOverrides(false)
            .build()
            .show()
    }

    private fun startDirectionalLongPress(direction: Int) {
        val exoPlayer = player ?: return
        stopDirectionalLongPress()

        directionalLongPressActive = true
        longPressDirection = if (direction < 0) -1 else 1
        longPressRestoreSpeed = selectedPlaybackSpeed
        longPressRestorePlayWhenReady = exoPlayer.playWhenReady

        if (longPressDirection > 0) {
            exoPlayer.setPlaybackSpeed(VideoSpeedOptions.longPressSpeed)
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
        if (settingsManager.alwaysStartVideosFromBeginning()) {
            playbackPosition = 0L
            return
        }
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

    private fun currentPlayableMediaItem(): PlayableMediaItem {
        val uri = mediaUri()
        return PlayableMediaItem(
            uri = uri,
            title = intent.getStringExtra(EXTRA_MEDIA_TITLE),
            mimeType = intent.getStringExtra(EXTRA_MIME_TYPE),
            source = if (isLocalMediaUri(uri)) {
                PlayableMediaSource.LOCAL_DOCUMENT
            } else {
                PlayableMediaSource.REMOTE_URL
            },
            userAgent = intent.getStringExtra(EXTRA_USER_AGENT),
            referer = intent.getStringExtra(EXTRA_REFERER),
            headers = requestHeaders(),
            subtitleCandidates = subtitleCandidatesFromIntent()
        )
    }

    private fun toMediaItem(item: PlayableMediaItem): MediaItem {
        return MediaItem.Builder()
            .setUri(Uri.parse(item.uri))
            .setMimeType(normalizedMimeType(item.mimeType))
            .setSubtitleConfigurations(
                item.subtitleCandidates.map(::toSubtitleConfiguration)
            )
            .build()
    }

    private fun toSubtitleConfiguration(
        candidate: ExternalSubtitleCandidate
    ): MediaItem.SubtitleConfiguration {
        val builder = MediaItem.SubtitleConfiguration.Builder(Uri.parse(candidate.uri))
        candidate.mimeType?.takeIf { it.isNotBlank() }?.let(builder::setMimeType)
        candidate.language?.takeIf { it.isNotBlank() }?.let(builder::setLanguage)
        candidate.label?.takeIf { it.isNotBlank() }?.let(builder::setLabel)
        return builder.build()
    }

    private fun isLocalMediaUri(uri: String): Boolean {
        return uri.startsWith("content:", ignoreCase = true) ||
            uri.startsWith("file:", ignoreCase = true)
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
        return playbackQueue.items.getOrNull(currentMediaItemIndex)?.uri?.trim()
            ?: mediaUri().trim()
    }

    private fun isPrivateBrowsingPlayback(): Boolean {
        return intent.getBooleanExtra(EXTRA_PRIVATE_BROWSING, false)
    }

    private fun subtitleCandidatesFromIntent(): List<ExternalSubtitleCandidate> {
        val uris = intent.getStringArrayListExtra(EXTRA_SUBTITLE_URIS).orEmpty()
        val labels = intent.getStringArrayListExtra(EXTRA_SUBTITLE_LABELS).orEmpty()
        val mimeTypes = intent.getStringArrayListExtra(EXTRA_SUBTITLE_MIME_TYPES).orEmpty()
        val languages = intent.getStringArrayListExtra(EXTRA_SUBTITLE_LANGUAGES).orEmpty()

        return uris.mapIndexedNotNull { index, uri ->
            val normalizedUri = uri.takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null
            ExternalSubtitleCandidate(
                uri = normalizedUri,
                label = labels.getOrNull(index)?.takeIf { it.isNotBlank() },
                mimeType = mimeTypes.getOrNull(index)?.takeIf { it.isNotBlank() },
                language = languages.getOrNull(index)?.takeIf { it.isNotBlank() }
            )
        }
    }

    private fun playbackQueueFromIntent(): PlaybackQueue {
        val encodedQueue = intent.getStringExtra(EXTRA_PLAYBACK_QUEUE)
        return encodedQueue?.let(::decodePlaybackQueue)
            ?: PlaybackQueue.single(currentPlayableMediaItem())
    }

    private fun decodePlaybackQueue(encodedQueue: String): PlaybackQueue? {
        return runCatching {
            val root = JSONObject(encodedQueue)
            val itemArray = root.getJSONArray("items")
            val items = (0 until itemArray.length()).mapNotNull { index ->
                itemArray.optJSONObject(index)?.toPlayableMediaItem()
            }
            if (items.isEmpty()) {
                return@runCatching null
            }
            val index = root.optInt("currentIndex", 0).coerceIn(0, items.lastIndex)
            val repeat = root.optString("repeatMode")
                .takeIf { it.isNotBlank() }
                ?.let { runCatching { PlaybackRepeatMode.valueOf(it) }.getOrNull() }
                ?: PlaybackRepeatMode.NONE
            val originalItems = playableItemsFromJson(root.optJSONArray("originalItems"))
                .takeIf { it.isNotEmpty() }
            PlaybackQueue(
                items = items,
                currentIndex = index,
                repeatMode = repeat,
                originalItems = originalItems
            )
        }.getOrNull()
    }

    private fun playableItemsFromJson(array: JSONArray?): List<PlayableMediaItem> {
        if (array == null) {
            return emptyList()
        }
        return (0 until array.length()).mapNotNull { index ->
            array.optJSONObject(index)?.toPlayableMediaItem()
        }
    }

    private fun JSONObject.toPlayableMediaItem(): PlayableMediaItem? {
        val uri = optString("uri").takeIf { it.isNotBlank() } ?: return null
        return PlayableMediaItem(
            uri = uri,
            title = optString("title").takeIf { it.isNotBlank() },
            mimeType = optString("mimeType").takeIf { it.isNotBlank() },
            source = PlaybackQueueJson.sourceFromName(optString("source")),
            userAgent = optString("userAgent").takeIf { it.isNotBlank() },
            referer = optString("referer").takeIf { it.isNotBlank() },
            subtitleCandidates = subtitleArrayFromJson(optJSONArray("subtitles"))
        )
    }

    private fun subtitleArrayFromJson(array: JSONArray?): List<ExternalSubtitleCandidate> {
        if (array == null) {
            return emptyList()
        }
        return (0 until array.length()).mapNotNull { index ->
            val subtitle = array.optJSONObject(index) ?: return@mapNotNull null
            val uri = subtitle.optString("uri").takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            ExternalSubtitleCandidate(
                uri = uri,
                label = subtitle.optString("label").takeIf { it.isNotBlank() },
                mimeType = subtitle.optString("mimeType").takeIf { it.isNotBlank() },
                language = subtitle.optString("language").takeIf { it.isNotBlank() }
            )
        }
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
        private const val EXTRA_SUBTITLE_URIS = "com.example.videobrowser.extra.SUBTITLE_URIS"
        private const val EXTRA_SUBTITLE_LABELS = "com.example.videobrowser.extra.SUBTITLE_LABELS"
        private const val EXTRA_SUBTITLE_MIME_TYPES =
            "com.example.videobrowser.extra.SUBTITLE_MIME_TYPES"
        private const val EXTRA_SUBTITLE_LANGUAGES =
            "com.example.videobrowser.extra.SUBTITLE_LANGUAGES"
        private const val EXTRA_PLAYBACK_QUEUE = "com.example.videobrowser.extra.PLAYBACK_QUEUE"
        private const val STATE_PLAYBACK_POSITION = "playback_position"
        private const val STATE_PLAY_WHEN_READY = "play_when_ready"
        private const val STATE_MEDIA_ITEM_INDEX = "media_item_index"
        private const val STATE_LANDSCAPE = "landscape"
        private const val STATE_PLAYBACK_SPEED = "playback_speed"
        private const val STATE_REPEAT_MODE = "repeat_mode"
        private const val STATE_PLAYBACK_QUEUE = "playback_queue"
        private const val STATE_VIDEO_ZOOM_MODE = "video_zoom_mode"
        private const val STATE_VIDEO_EFFECTS_ENABLED = "video_effects_enabled"
        private const val STATE_RETRIED_WITHOUT_VIDEO_EFFECTS = "retried_without_video_effects"
        private const val VIDEO_LOG_TAG = "VideoBrowserVideo"
        private const val DEFAULT_PLAYBACK_SPEED = 1f
        private const val CONTROLS_HIDE_DELAY_MS = 3000
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
            privateBrowsing: Boolean = false,
            subtitleCandidates: List<ExternalSubtitleCandidate> = emptyList(),
            playbackQueue: PlaybackQueue? = null
        ): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_MEDIA_URI, mediaUri)
                putExtra(EXTRA_MEDIA_TITLE, title)
                putExtra(EXTRA_MIME_TYPE, mimeType)
                putExtra(EXTRA_USER_AGENT, userAgent)
                putExtra(EXTRA_COOKIE, cookie)
                putExtra(EXTRA_REFERER, referer)
                putExtra(EXTRA_PRIVATE_BROWSING, privateBrowsing)
                if (subtitleCandidates.isNotEmpty()) {
                    putStringArrayListExtra(
                        EXTRA_SUBTITLE_URIS,
                        ArrayList(subtitleCandidates.map { it.uri })
                    )
                    putStringArrayListExtra(
                        EXTRA_SUBTITLE_LABELS,
                        ArrayList(subtitleCandidates.map { it.label.orEmpty() })
                    )
                    putStringArrayListExtra(
                        EXTRA_SUBTITLE_MIME_TYPES,
                        ArrayList(subtitleCandidates.map { it.mimeType.orEmpty() })
                    )
                    putStringArrayListExtra(
                        EXTRA_SUBTITLE_LANGUAGES,
                        ArrayList(subtitleCandidates.map { it.language.orEmpty() })
                    )
                }
                playbackQueue?.let {
                    putExtra(EXTRA_PLAYBACK_QUEUE, PlaybackQueueJson.encode(it))
                }
            }
        }

        private object PlaybackQueueJson {
            fun encode(queue: PlaybackQueue): String {
                return JSONObject()
                    .put("currentIndex", queue.currentIndex)
                    .put("repeatMode", queue.repeatMode.name)
                    .put(
                        "items",
                        JSONArray().apply {
                            queue.items.forEach { item -> put(item.toJson()) }
                        }
                    )
                    .apply {
                        queue.originalItems?.let { originalItems ->
                            put(
                                "originalItems",
                                JSONArray().apply {
                                    originalItems.forEach { item -> put(item.toJson()) }
                                }
                            )
                        }
                    }
                    .toString()
            }

            fun sourceFromName(name: String): PlayableMediaSource {
                return runCatching { PlayableMediaSource.valueOf(name) }
                    .getOrDefault(PlayableMediaSource.REMOTE_URL)
            }

            private fun PlayableMediaItem.toJson(): JSONObject {
                return JSONObject()
                    .put("uri", uri)
                    .put("title", title.orEmpty())
                    .put("mimeType", mimeType.orEmpty())
                    .put("source", source.name)
                    .put("userAgent", userAgent.orEmpty())
                    .put("referer", referer.orEmpty())
                    .put(
                        "subtitles",
                        JSONArray().apply {
                            subtitleCandidates.forEach { put(it.toJson()) }
                        }
                    )
            }

            private fun ExternalSubtitleCandidate.toJson(): JSONObject {
                return JSONObject()
                    .put("uri", uri)
                    .put("label", label.orEmpty())
                    .put("mimeType", mimeType.orEmpty())
                    .put("language", language.orEmpty())
            }
        }
    }
}
