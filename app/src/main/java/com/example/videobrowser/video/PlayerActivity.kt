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
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.AudioAttributes
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.example.videobrowser.R
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.utils.PlaybackSpeedNormalizer

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
    private lateinit var nativePlaybackHistoryController: NativePlaybackHistoryController
    private lateinit var playerControlsVisibilityController: NativePlayerControlsVisibilityController
    private lateinit var playbackQueue: PlaybackQueue
    private var player: ExoPlayer? = null
    private val nativePlayerWindowController by lazy {
        NativePlayerWindowController(this)
    }
    private val trackSelectionDialogController by lazy {
        NativeTrackSelectionDialogController(
            activity = this,
            playerProvider = { player },
            wakePlayerControls = ::wakePlayerControls,
            onTrackTypeSelected = { trackType ->
                handlePlaybackCommand(PlaybackCommand.SelectTrack(trackType))
            }
        )
    }
    private val playbackQueueDialogController by lazy {
        NativePlaybackQueueDialogController(
            activity = this,
            queueProvider = { playbackQueue },
            wakePlayerControls = ::wakePlayerControls,
            onSelectQueueItem = { index ->
                handlePlaybackCommand(PlaybackCommand.SelectQueueItem(index))
            },
            onToggleShuffle = {
                handlePlaybackCommand(PlaybackCommand.ToggleShuffle)
            },
            onRemoveMedia = ::removeMediaFromQueue
        )
    }
    private val nativePlayerTransportController = NativePlayerTransportController(
        player = { player },
        logTag = VIDEO_LOG_TAG,
        wakePlayerControls = ::wakePlayerControls
    )
    private val directionalLongPressController = NativeDirectionalLongPressController(
        scheduler = HandlerPlaybackScanScheduler(Handler(Looper.getMainLooper())),
        seekBy = nativePlayerTransportController::seekBy
    )
    private var playbackPosition = 0L
    private var playWhenReady = true
    private var currentMediaItemIndex = 0
    private var selectedPlaybackSpeed = DEFAULT_PLAYBACK_SPEED
    private var isLandscape = true
    private var videoEffectsEnabled = true
    private var retriedPlaybackWithoutVideoEffects = false
    private var repeatMode = PlaybackRepeatMode.NONE
    private var videoZoomMode = VideoZoomMode.FIT
    private val intentReader: PlayerIntentReader by lazy { PlayerIntentReader(intent) }

    /**
     * 函数 `onCreate`：处理 `on Create` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param savedInstanceState 参数类型为 `Bundle?`，表示函数执行 `savedInstanceState` 相关逻辑时需要读取或处理的输入。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 播放器默认横屏，并把系统音量键绑定到媒体音量。
        nativePlayerWindowController.applyOrientation(isLandscape = true)
        volumeControlStream = AudioManager.STREAM_MUSIC
        setContentView(R.layout.activity_player)
        val preferenceStore = PreferenceStore.from(this)
        settingsManager = SettingsManager(preferenceStore)
        playbackHistoryRepository = PlaybackHistoryRepository(preferenceStore)
        nativePlaybackHistoryController = NativePlaybackHistoryController(
            alwaysStartVideosFromBeginning = settingsManager::alwaysStartVideosFromBeginning,
            progressFor = playbackHistoryRepository::progressFor,
            resumePositionFor = playbackHistoryRepository::resumePositionFor,
            saveProgress = { progress, privateBrowsing ->
                playbackHistoryRepository.save(progress, privateBrowsing)
            },
            privateBrowsing = { intentReader.isPrivateBrowsing() }
        )
        val intentPlaybackQueue = intentReader.playbackQueue()
        val defaultPlaybackSpeed = settingsManager.defaultVideoSpeed()
        val restoredState = NativePlayerSavedState.restore(
            savedInstanceState = savedInstanceState,
            fallbackPlaybackQueue = intentPlaybackQueue,
            fallbackPlaybackSpeed = defaultPlaybackSpeed,
            fallbackVideoZoomMode = videoZoomMode
        )
        playbackQueue = restoredState?.playbackQueue ?: intentPlaybackQueue
        currentMediaItemIndex = restoredState?.currentMediaItemIndex ?: playbackQueue.currentIndex
        repeatMode = restoredState?.repeatMode ?: playbackQueue.repeatMode
        selectedPlaybackSpeed = restoredState?.selectedPlaybackSpeed ?: defaultPlaybackSpeed

        playerRoot = findViewById(R.id.playerRoot)
        playerView = findViewById(R.id.playerView)
        playerControlsVisibilityController = NativePlayerControlsVisibilityController(
            playerView = playerView,
            playerProvider = { player },
            logTag = VIDEO_LOG_TAG
        )
        playerView.keepScreenOn = true
        playerControlsVisibilityController.applyDefaultHideTimeout()

        if (restoredState != null) {
            // 系统重建时恢复内存状态，避免旋转或后台回收后从头播放。
            playbackPosition = restoredState.playbackPosition
            playWhenReady = restoredState.playWhenReady
            isLandscape = restoredState.isLandscape
            videoZoomMode = restoredState.videoZoomMode
            videoEffectsEnabled = restoredState.videoEffectsEnabled
            retriedPlaybackWithoutVideoEffects = restoredState.retriedPlaybackWithoutVideoEffects
        } else {
            restorePlaybackHistory()
        }
        applyRequestedOrientation()
        applyVideoZoomMode()
        setupGestureOverlay()

        if (intentReader.mediaUri().isBlank()) {
            Toast.makeText(this, R.string.toast_media_url_invalid, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        title = intentReader.mediaTitle().orEmpty()
        hideSystemBars()
    }

    /**
     * 函数 `dispatchKeyEvent`：封装 `dispatch Key Event` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param event 参数类型为 `KeyEvent`，表示函数执行 `event` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            wakePlayerControls()
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * 函数 `onStart`：处理 `on Start` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    /**
     * 函数 `onResume`：处理 `on Resume` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onResume() {
        super.onResume()
        if (::gestureOverlay.isInitialized) {
            gestureOverlay.showOverlay()
        }
        wakePlayerControls()
        hideSystemBars()
    }

    /**
     * 函数 `onPause`：处理 `on Pause` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onPause() {
        if (::gestureOverlay.isInitialized) {
            gestureOverlay.hideOverlay()
        }
        super.onPause()
    }

    /**
     * 函数 `onWindowFocusChanged`：处理 `on Window Focus Changed` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param hasFocus 参数类型为 `Boolean`，表示函数执行 `hasFocus` 相关逻辑时需要读取或处理的输入。
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    /**
     * 函数 `onSaveInstanceState`：处理 `on Save Instance State` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param outState 参数类型为 `Bundle`，表示函数执行 `outState` 相关逻辑时需要读取或处理的输入。
     */
    override fun onSaveInstanceState(outState: Bundle) {
        savePlayerState()
        val sessionState = currentPlaybackSessionState()
        NativePlayerSavedState.save(
            outState = outState,
            sessionState = sessionState,
            playbackQueue = playbackQueue,
            isLandscape = isLandscape,
            videoEffectsEnabled = videoEffectsEnabled,
            retriedPlaybackWithoutVideoEffects = retriedPlaybackWithoutVideoEffects
        )
        super.onSaveInstanceState(outState)
    }

    /**
     * 函数 `onStop`：处理 `on Stop` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onStop() {
        releasePlayer()
        super.onStop()
    }

    /**
     * 函数 `initializePlayer`：封装 `initialize Player` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun initializePlayer() {
        if (player != null) {
            Log.d(VIDEO_LOG_TAG, "event=native-initialize skipped=true")
            return
        }
        Log.d(
            VIDEO_LOG_TAG,
            "event=native-initialize uri=${intentReader.mediaUri().take(180)} mime=${intentReader.mimeType()}"
        )

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(intentReader.requestHeaders())
        // WebView 传来的 User-Agent、Cookie、Referer 会放进请求头，减少站点防盗链导致的播放失败。
        intentReader.userAgent()
            ?.takeIf { it.isNotBlank() }
            ?.let { dataSourceFactory.setUserAgent(it) }

        val mediaSourceFactory = DefaultMediaSourceFactory(
            DefaultDataSource.Factory(this, dataSourceFactory)
        )
        val mediaItems = playbackQueue.items.map(PlayableMediaItemMedia3Converter::toMediaItem)
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
                    NativePlayerEventListener(
                        logTag = VIDEO_LOG_TAG,
                        isVideoEffectsEnabled = { videoEffectsEnabled },
                        hasRetriedPlaybackWithoutVideoEffects = {
                            retriedPlaybackWithoutVideoEffects
                        },
                        retryPlaybackWithoutVideoEffects = ::retryPlaybackWithoutVideoEffects,
                        showPlaybackFailed = {
                            Toast.makeText(
                                this@PlayerActivity,
                                R.string.toast_media_playback_failed,
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        savePlaybackHistory = { savePlaybackHistory(exoPlayer) },
                        wakePlayerControls = ::wakePlayerControls,
                        mediaItemTransitioned = {
                            handleMediaItemTransition(exoPlayer.currentMediaItemIndex)
                        }
                    )
                )
                playerView.player = exoPlayer
                exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true)
                exoPlayer.setMediaItems(mediaItems, currentMediaItemIndex, playbackPosition)
                exoPlayer.repeatMode = PlaybackRepeatModeMedia3Converter.toPlayerRepeatMode(repeatMode)
                exoPlayer.setPlaybackSpeed(selectedPlaybackSpeed)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
            }
    }

    /**
     * 函数 `retryPlaybackWithoutVideoEffects`：封装 `retry Playback Without Video Effects` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    private fun handleMediaItemTransition(index: Int) {
        currentMediaItemIndex = index
        playbackQueue = playbackQueue
            .select(currentMediaItemIndex)
            .copy(repeatMode = repeatMode)
        playbackPosition = 0L
        title = playbackQueue.items.getOrNull(currentMediaItemIndex)?.title.orEmpty()
        updateQueueControls()
    }

    /**
     * 函数 `setupGestureOverlay`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupGestureOverlay() {
        // 手势层不直接操作 ExoPlayer，而是统一发 PlaybackCommand，再由 handlePlaybackCommand 分发。
        gestureOverlay = FullscreenVideoGestureOverlay(this).apply {
            onSeekBy = { offsetMs -> handlePlaybackCommand(PlaybackCommand.SeekBy(offsetMs)) }
            onSeekTo = { positionMs -> handlePlaybackCommand(PlaybackCommand.SeekTo(positionMs)) }
            onSeekPreviewStart = nativePlayerTransportController::currentSeekPosition
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
            arePlaybackControlsVisible = ::arePlayerControlsVisible
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
            setQueueControlsVisible(playbackQueue.hasMultipleItems)
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

    /**
     * 函数 `handlePlaybackCommand`：处理 `handle Playback Command` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param command 参数类型为 `PlaybackCommand`，表示函数执行 `command` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun handlePlaybackCommand(command: PlaybackCommand): Any? {
        // 这一层把 UI 手势/按钮命令转换成播放器动作，后续新增控制项时也从这里接入。
        return when (command) {
            PlaybackCommand.Play -> nativePlayerTransportController.play()
            PlaybackCommand.Pause -> nativePlayerTransportController.pause()
            PlaybackCommand.TogglePlayPause -> nativePlayerTransportController.togglePlayPause()
            is PlaybackCommand.SeekBy -> {
                nativePlayerTransportController.seekBy(command.offsetMs)
                Unit
            }
            is PlaybackCommand.SeekTo -> {
                nativePlayerTransportController.seekTo(command.positionMs)
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
                playbackQueueDialogController.showMenu()
                Unit
            }
            PlaybackCommand.ToggleShuffle -> toggleShuffleMode()
            PlaybackCommand.CycleZoom -> cycleVideoZoomMode()
            PlaybackCommand.ShowTrackSelection -> {
                trackSelectionDialogController.showMenu()
                Unit
            }
            is PlaybackCommand.SelectTrack -> {
                trackSelectionDialogController.showDialog(command.trackType)
                Unit
            }
        }
    }

    /**
     * 函数 `currentPlaybackSessionState`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun currentPlaybackSessionState(): PlaybackSessionState {
        val exoPlayer = player
        val durationMs = exoPlayer
            ?.duration
            ?.let(Media3Duration::knownDurationMs)
        return PlaybackSessionState.fromQueue(
            queue = playbackQueue,
            positionMs = exoPlayer?.currentPosition ?: playbackPosition,
            durationMs = durationMs,
            speed = selectedPlaybackSpeed,
            playWhenReady = exoPlayer?.playWhenReady ?: playWhenReady,
            zoomMode = videoZoomMode
        )
    }

    /**
     * 函数 `wakePlayerControls`：封装 `wake Player Controls` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun wakePlayerControls() {
        if (!::playerView.isInitialized) {
            return
        }
        playerControlsVisibilityController.wakeControls()
    }

    /**
     * 函数 `arePlayerControlsVisible`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun arePlayerControlsVisible(): Boolean {
        return ::playerView.isInitialized && playerControlsVisibilityController.areControlsVisible()
    }

    /**
     * 函数 `setPlayerPlaybackSpeed`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param speed 参数类型为 `Float`，表示函数执行 `speed` 相关逻辑时需要读取或处理的输入。
     */
    private fun setPlayerPlaybackSpeed(speed: Float) {
        selectedPlaybackSpeed = PlaybackSpeedNormalizer.normalize(speed)
        settingsManager.setDefaultVideoSpeed(selectedPlaybackSpeed)
        player?.setPlaybackSpeed(selectedPlaybackSpeed)
    }

    /**
     * 函数 `playPreviousMedia`：封装 `play Previous Media` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun playPreviousMedia() {
        playMediaAt(playbackQueue.previous().currentIndex)
    }

    /**
     * 函数 `playNextMedia`：封装 `play Next Media` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun playNextMedia() {
        playMediaAt(playbackQueue.next().currentIndex)
    }

    /**
     * 函数 `playMediaAt`：封装 `play Media At` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param index 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
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

    /**
     * 函数 `cycleRepeatMode`：封装 `cycle Repeat Mode` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun cycleRepeatMode(): PlaybackRepeatMode {
        repeatMode = repeatMode.next()
        playbackQueue = playbackQueue.copy(repeatMode = repeatMode)
        player?.repeatMode = PlaybackRepeatModeMedia3Converter.toPlayerRepeatMode(repeatMode)
        updateQueueControls()
        return repeatMode
    }

    /**
     * 函数 `updateQueueControls`：根据最新状态刷新 `update Queue Controls` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun updateQueueControls() {
        if (!::gestureOverlay.isInitialized) {
            return
        }
        gestureOverlay.setQueueControlsVisible(playbackQueue.hasMultipleItems)
        gestureOverlay.setRepeatMode(repeatMode)
    }

    /**
     * 函数 `cycleVideoZoomMode`：封装 `cycle Video Zoom Mode` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun cycleVideoZoomMode(): VideoZoomMode {
        videoZoomMode = videoZoomMode.next()
        applyVideoZoomMode()
        wakePlayerControls()
        return videoZoomMode
    }

    /**
     * 函数 `applyVideoZoomMode`：根据最新状态刷新 `apply Video Zoom Mode` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun applyVideoZoomMode() {
        if (::playerView.isInitialized) {
            playerView.resizeMode = videoZoomMode.resizeMode
        }
        if (::gestureOverlay.isInitialized) {
            gestureOverlay.setVideoZoomMode(videoZoomMode)
        }
    }

    /**
     * 函数 `toggleShuffleMode`：封装 `toggle Shuffle Mode` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun toggleShuffleMode(): Boolean {
        if (!playbackQueue.hasMultipleItems) {
            wakePlayerControls()
            return playbackQueue.isShuffled
        }
        savePlayerState()
        playbackQueue = playbackQueue.toggleShuffle()
        currentMediaItemIndex = playbackQueue.currentIndex
        syncPlayerQueueToPlaybackQueue()
        updateQueueControls()
        wakePlayerControls()
        return playbackQueue.isShuffled
    }

    /**
     * 函数 `syncPlayerQueueToPlaybackQueue`：根据最新状态刷新 `sync Player Queue To Playback Queue` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun syncPlayerQueueToPlaybackQueue() {
        val exoPlayer = player ?: return
        exoPlayer.setMediaItems(
            playbackQueue.items.map(PlayableMediaItemMedia3Converter::toMediaItem),
            currentMediaItemIndex,
            playbackPosition
        )
        exoPlayer.repeatMode = PlaybackRepeatModeMedia3Converter.toPlayerRepeatMode(repeatMode)
        exoPlayer.setPlaybackSpeed(selectedPlaybackSpeed)
        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.prepare()
    }

    /**
     * 函数 `removeMediaFromQueue`：封装 `remove Media From Queue` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param index 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    private fun removeMediaFromQueue(index: Int) {
        if (!playbackQueue.canRemoveAt(index)) {
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

    /**
     * 函数 `startDirectionalLongPress`：启动或加载 `start Directional Long Press` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param direction 参数类型为 `Int`，表示函数执行 `direction` 相关逻辑时需要读取或处理的输入。
     */
    private fun startDirectionalLongPress(direction: Int) {
        val exoPlayer = player ?: return
        directionalLongPressController.start(
            direction = direction,
            selectedPlaybackSpeed = selectedPlaybackSpeed,
            playbackTarget = ExoPlayerDirectionalLongPressTarget(exoPlayer)
        )
    }

    /**
     * 函数 `stopDirectionalLongPress`：封装 `stop Directional Long Press` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun stopDirectionalLongPress() {
        directionalLongPressController.stop()?.let { restoredSpeed ->
            selectedPlaybackSpeed = restoredSpeed
        }
    }

    /**
     * 函数 `togglePlayerOrientation`：封装 `toggle Player Orientation` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun togglePlayerOrientation(): Boolean {
        isLandscape = !isLandscape
        applyRequestedOrientation()
        return isLandscape
    }

    /**
     * 函数 `applyRequestedOrientation`：根据最新状态刷新 `apply Requested Orientation` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun applyRequestedOrientation() {
        nativePlayerWindowController.applyOrientation(isLandscape)
        if (::gestureOverlay.isInitialized) {
            gestureOverlay.setLandscape(isLandscape)
        }
    }

    /**
     * 函数 `releasePlayer`：封装 `release Player` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun releasePlayer() {
        stopDirectionalLongPress()
        savePlayerState()
        playerView.player = null
        player?.release()
        player = null
    }

    /**
     * 函数 `savePlayerState`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun savePlayerState() {
        player?.let {
            playbackPosition = it.currentPosition
            playWhenReady = it.playWhenReady
            currentMediaItemIndex = it.currentMediaItemIndex
            savePlaybackHistory(it)
        }
    }

    /**
     * 函数 `restorePlaybackHistory`：封装 `restore Playback History` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun restorePlaybackHistory() {
        val restored = nativePlaybackHistoryController.restore(playbackHistoryIdentity())
        restored.positionMs?.let { positionMs ->
            playbackPosition = positionMs
        }
        restored.playbackSpeed?.let { playbackSpeed ->
            selectedPlaybackSpeed = playbackSpeed
        }
    }

    /**
     * 函数 `savePlaybackHistory`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param exoPlayer 参数类型为 `ExoPlayer`，表示函数执行 `exoPlayer` 相关逻辑时需要读取或处理的输入。
     */
    private fun savePlaybackHistory(exoPlayer: ExoPlayer) {
        nativePlaybackHistoryController.save(
            NativePlaybackHistorySnapshot(
                mediaIdentity = playbackHistoryIdentity(),
                positionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                durationMs = Media3Duration.durationOrZero(exoPlayer.duration),
                speed = selectedPlaybackSpeed,
                title = playbackQueue.items.getOrNull(currentMediaItemIndex)?.title
                    ?: intentReader.mediaTitle()
            )
        )
    }

    /**
     * 函数 `hideSystemBars`：控制 `hide System Bars` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun hideSystemBars() {
        nativePlayerWindowController.hideSystemBars()
    }

    /**
     * 函数 `playbackHistoryIdentity`：封装 `playback History Identity` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun playbackHistoryIdentity(): String {
        return playbackQueue.items.getOrNull(currentMediaItemIndex)?.uri?.trim()
            ?: intentReader.mediaUri().trim()
    }

    companion object {
        private const val VIDEO_LOG_TAG = "VideoBrowserVideo"
        private const val DEFAULT_PLAYBACK_SPEED = 1f
        /**
         * 函数 `createIntent`：创建 `create Intent` 需要的对象、视图或配置，并返回给后续流程使用。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param context 参数类型为 `Context`，表示 Android 上下文，用来读取资源、启动系统服务或访问应用环境。
         * @param mediaUri 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
         * @param title 参数类型为 `String?`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param mimeType 参数类型为 `String?`，表示函数执行 `mimeType` 相关逻辑时需要读取或处理的输入。
         * @param userAgent 参数类型为 `String?`，表示函数执行 `userAgent` 相关逻辑时需要读取或处理的输入。
         * @param cookie 参数类型为 `String?`，表示函数执行 `cookie` 相关逻辑时需要读取或处理的输入。
         * @param referer 参数类型为 `String?`，表示函数执行 `referer` 相关逻辑时需要读取或处理的输入。
         * @param privateBrowsing 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
         * @param subtitleCandidates 参数类型为 `List<ExternalSubtitleCandidate>`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param playbackQueue 参数类型为 `PlaybackQueue?`，表示函数执行 `playbackQueue` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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
            return PlayerIntentFactory.create(
                context = context,
                mediaUri = mediaUri,
                title = title,
                mimeType = mimeType,
                userAgent = userAgent,
                cookie = cookie,
                referer = referer,
                privateBrowsing = privateBrowsing,
                subtitleCandidates = subtitleCandidates,
                playbackQueue = playbackQueue
            )
        }
    }
}
