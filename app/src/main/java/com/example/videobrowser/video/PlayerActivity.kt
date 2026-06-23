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
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.videobrowser.R
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.storage.PreferenceStore
import com.example.videobrowser.utils.ShortToast

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
    private lateinit var nativePlaybackHistorySessionController: NativePlaybackHistorySessionController
    private lateinit var playerControlsVisibilityController: NativePlayerControlsVisibilityController
    private lateinit var playbackQueue: PlaybackQueue
    private val playbackState = NativePlayerPlaybackState()
    private var player: ExoPlayer? = null
    private val nativePlayerWindowController by lazy {
        NativePlayerWindowController(this)
    }
    private val nativePlayerOrientationController by lazy {
        NativePlayerOrientationController(
            windowController = nativePlayerWindowController,
            gestureOverlay = { if (::gestureOverlay.isInitialized) gestureOverlay else null }
        )
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
    private val nativePlayerTransportController = NativePlayerTransportController(
        player = { player },
        logTag = VIDEO_LOG_TAG,
        wakePlayerControls = ::wakePlayerControls
    )
    private val nativePlayerVideoZoomController = NativePlayerVideoZoomController(
        playerView = { if (::playerView.isInitialized) playerView else null },
        gestureOverlay = { if (::gestureOverlay.isInitialized) gestureOverlay else null },
        wakePlayerControls = ::wakePlayerControls
    )
    private val nativePlayerRepeatModeController = NativePlayerRepeatModeController(
        playerProvider = { player },
        gestureOverlay = { if (::gestureOverlay.isInitialized) gestureOverlay else null }
    )
    private val nativePlayerPlaybackSpeedController = NativePlayerPlaybackSpeedController(
        playerProvider = { player },
        gestureOverlay = { if (::gestureOverlay.isInitialized) gestureOverlay else null },
        saveDefaultVideoSpeed = { speed -> settingsManager.setDefaultVideoSpeed(speed) }
    )
    private val nativePlayerVideoEffectsController = NativePlayerVideoEffectsController(
        logTag = VIDEO_LOG_TAG
    )
    private val nativePlaybackCommandDispatcher by lazy {
        NativePlaybackCommandDispatcher(
            transportController = nativePlayerTransportController,
            playbackSpeedController = nativePlayerPlaybackSpeedController,
            queueController = nativePlayerQueueController,
            playbackQueue = { playbackQueue },
            setPlaybackQueue = { queue -> playbackQueue = queue },
            repeatModeController = nativePlayerRepeatModeController,
            videoZoomController = nativePlayerVideoZoomController,
            trackSelectionDialogController = trackSelectionDialogController,
            playbackQueueDialogController = playbackQueueDialogController,
            updateQueueControls = ::updateQueueControls
        )
    }
    private val nativePlaybackSessionStateProvider by lazy {
        NativePlaybackSessionStateProvider(
            playerProvider = { player },
            playbackQueue = { playbackQueue },
            fallbackPlaybackPosition = { playbackState.playbackPosition },
            fallbackPlayWhenReady = { playbackState.playWhenReady },
            currentPlaybackSpeed = nativePlayerPlaybackSpeedController::currentSpeed,
            currentVideoZoomMode = nativePlayerVideoZoomController::currentMode
        )
    }
    private val nativePlayerInitializer by lazy {
        NativePlayerInitializer(
            context = this,
            playerView = playerView,
            logTag = VIDEO_LOG_TAG,
            mediaUri = intentReader::mediaUri,
            mimeType = intentReader::mimeType,
            requestHeaders = intentReader::requestHeaders,
            userAgent = intentReader::userAgent,
            playbackQueue = { playbackQueue },
            currentMediaItemIndex = { playbackState.currentMediaItemIndex },
            playbackPosition = { playbackState.playbackPosition },
            playWhenReady = { playbackState.playWhenReady },
            applyVideoEffects = nativePlayerVideoEffectsController::applyToPlayer,
            applyRepeatMode = nativePlayerRepeatModeController::applyToPlayer,
            applyPlaybackSpeed = nativePlayerPlaybackSpeedController::applyToPlayer,
            createEventListener = ::createNativePlayerEventListener
        )
    }
    private val nativePlayerQueueController by lazy {
        NativePlayerQueueController(
            playerProvider = { player },
            queueProvider = { playbackQueue },
            setQueue = { queue -> playbackQueue = queue },
            currentMediaItemIndex = { playbackState.currentMediaItemIndex },
            setCurrentMediaItemIndex = playbackState::setCurrentMediaItemIndex,
            playbackPosition = { playbackState.playbackPosition },
            setPlaybackPosition = playbackState::setPlaybackPosition,
            playWhenReady = { playbackState.playWhenReady },
            savePlayerState = ::savePlayerState,
            restorePlaybackPositionForCurrentMedia = ::restorePlaybackPositionForCurrentMedia,
            applyRepeatModeToQueue = nativePlayerRepeatModeController::applyToQueue,
            applyRepeatModeToPlayer = nativePlayerRepeatModeController::applyToPlayer,
            applyPlaybackSpeedToPlayer = nativePlayerPlaybackSpeedController::applyToPlayer,
            updateTitle = { mediaTitle -> title = mediaTitle },
            updateQueueControls = ::updateQueueControls,
            wakePlayerControls = ::wakePlayerControls
        )
    }
    private val nativePlayerGestureOverlayBinder by lazy {
        NativePlayerGestureOverlayBinder(
            activity = this,
            playerRoot = playerRoot,
            seekPreviewStart = nativePlayerTransportController::currentSeekPosition,
            handlePlaybackCommand = ::handlePlaybackCommand,
            startDirectionalLongPress = ::startDirectionalLongPress,
            stopDirectionalLongPress = ::stopDirectionalLongPress,
            toggleOrientation = nativePlayerOrientationController::toggle,
            wakePlayerControls = ::wakePlayerControls,
            arePlayerControlsVisible = ::arePlayerControlsVisible,
            exitFullscreen = ::finish,
            currentPlaybackSpeed = nativePlayerPlaybackSpeedController::currentSpeed,
            isLandscape = nativePlayerOrientationController::isLandscape,
            hasMultipleQueueItems = { playbackQueue.hasMultipleItems },
            currentRepeatMode = nativePlayerRepeatModeController::currentMode,
            currentVideoZoomMode = nativePlayerVideoZoomController::currentMode
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
            onRemoveMedia = nativePlayerQueueController::removeMediaFromQueue
        )
    }
    private val directionalLongPressController = NativeDirectionalLongPressController(
        scheduler = HandlerPlaybackScanScheduler(Handler(Looper.getMainLooper())),
        seekBy = nativePlayerTransportController::seekBy
    )
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
        nativePlayerOrientationController.setLandscape(isLandscape = true)
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
        nativePlaybackHistorySessionController = NativePlaybackHistorySessionController(
            historyController = nativePlaybackHistoryController,
            playbackQueue = { playbackQueue },
            currentMediaItemIndex = { playbackState.currentMediaItemIndex },
            fallbackMediaUri = intentReader::mediaUri,
            fallbackMediaTitle = intentReader::mediaTitle,
            currentPlaybackSpeed = nativePlayerPlaybackSpeedController::currentSpeed
        )
        val intentPlaybackQueue = intentReader.playbackQueue()
        val defaultPlaybackSpeed = settingsManager.defaultVideoSpeed()
        val restoredState = NativePlayerSavedState.restore(
            savedInstanceState = savedInstanceState,
            fallbackPlaybackQueue = intentPlaybackQueue,
            fallbackPlaybackSpeed = defaultPlaybackSpeed,
            fallbackVideoZoomMode = nativePlayerVideoZoomController.currentMode()
        )
        playbackQueue = restoredState?.playbackQueue ?: intentPlaybackQueue
        if (restoredState != null) {
            playbackState.restoreFrom(restoredState)
        } else {
            playbackState.setCurrentMediaItemIndex(playbackQueue.currentIndex)
        }
        nativePlayerRepeatModeController.setMode(restoredState?.repeatMode ?: playbackQueue.repeatMode)
        nativePlayerPlaybackSpeedController.restoreSpeed(
            restoredState?.selectedPlaybackSpeed ?: defaultPlaybackSpeed
        )

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
            nativePlayerOrientationController.setLandscape(restoredState.isLandscape)
            nativePlayerVideoZoomController.setMode(restoredState.videoZoomMode)
            nativePlayerVideoEffectsController.restoreState(
                enabled = restoredState.videoEffectsEnabled,
                retriedWithoutEffects = restoredState.retriedPlaybackWithoutVideoEffects
            )
        } else {
            restorePlaybackHistory()
        }
        nativePlayerOrientationController.apply()
        nativePlayerVideoZoomController.apply()
        setupGestureOverlay()

        if (intentReader.mediaUri().isBlank()) {
            ShortToast.show(this, R.string.toast_media_url_invalid)
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
        val sessionState = nativePlaybackSessionStateProvider.currentState()
        NativePlayerSavedState.save(
            outState = outState,
            sessionState = sessionState,
            playbackQueue = playbackQueue,
            isLandscape = nativePlayerOrientationController.isLandscape(),
            videoEffectsEnabled = nativePlayerVideoEffectsController.isEnabled(),
            retriedPlaybackWithoutVideoEffects = nativePlayerVideoEffectsController
                .hasRetriedWithoutEffects()
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
        // ExoPlayer 生命周期跟随 Activity：onStart 初始化，onStop 释放，播放位置由 savePlayerState 保存。
        player = nativePlayerInitializer.initialize()
    }

    private fun createNativePlayerEventListener(exoPlayer: ExoPlayer): NativePlayerEventListener {
        return NativePlayerEventListener(
            logTag = VIDEO_LOG_TAG,
            isVideoEffectsEnabled = nativePlayerVideoEffectsController::isEnabled,
            hasRetriedPlaybackWithoutVideoEffects =
                nativePlayerVideoEffectsController::hasRetriedWithoutEffects,
            retryPlaybackWithoutVideoEffects = ::retryPlaybackWithoutVideoEffects,
            showPlaybackFailed = {
                ShortToast.show(this@PlayerActivity, R.string.toast_media_playback_failed)
            },
            savePlaybackHistory = { savePlaybackHistory(exoPlayer) },
            wakePlayerControls = ::wakePlayerControls,
            mediaItemTransitioned = {
                nativePlayerQueueController.handleMediaItemTransition(
                    exoPlayer.currentMediaItemIndex
                )
            }
        )
    }

    /**
     * 函数 `retryPlaybackWithoutVideoEffects`：封装 `retry Playback Without Video Effects` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun retryPlaybackWithoutVideoEffects() {
        if (!nativePlayerVideoEffectsController.markRetryWithoutEffects()) {
            return
        }
        savePlayerState()
        releasePlayer()
        initializePlayer()
    }

    /**
     * 函数 `setupGestureOverlay`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupGestureOverlay() {
        gestureOverlay = nativePlayerGestureOverlayBinder.attach()
    }

    /**
     * 函数 `handlePlaybackCommand`：处理 `handle Playback Command` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param command 参数类型为 `PlaybackCommand`，表示函数执行 `command` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun handlePlaybackCommand(command: PlaybackCommand): Any? {
        return nativePlaybackCommandDispatcher.handle(command)
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
     * 函数 `updateQueueControls`：根据最新状态刷新 `update Queue Controls` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun updateQueueControls() {
        if (!::gestureOverlay.isInitialized) {
            return
        }
        gestureOverlay.setQueueControlsVisible(playbackQueue.hasMultipleItems)
        gestureOverlay.setRepeatMode(nativePlayerRepeatModeController.currentMode())
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
            selectedPlaybackSpeed = nativePlayerPlaybackSpeedController.currentSpeed(),
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
            nativePlayerPlaybackSpeedController.restoreSpeed(restoredSpeed)
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
            playbackState.updateFrom(it)
            savePlaybackHistory(it)
        }
    }

    /**
     * 函数 `restorePlaybackHistory`：封装 `restore Playback History` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun restorePlaybackHistory() {
        val restored = nativePlaybackHistorySessionController.restore()
        restored.positionMs?.let { positionMs ->
            playbackState.setPlaybackPosition(positionMs)
        }
        restored.playbackSpeed?.let { playbackSpeed ->
            nativePlayerPlaybackSpeedController.restoreSpeed(playbackSpeed)
        }
    }

    private fun restorePlaybackPositionForCurrentMedia(): Long {
        return nativePlaybackHistorySessionController.restorePositionForCurrentMedia()
    }

    /**
     * 函数 `savePlaybackHistory`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param exoPlayer 参数类型为 `ExoPlayer`，表示函数执行 `exoPlayer` 相关逻辑时需要读取或处理的输入。
     */
    private fun savePlaybackHistory(exoPlayer: ExoPlayer) {
        nativePlaybackHistorySessionController.save(exoPlayer)
    }

    /**
     * 函数 `hideSystemBars`：控制 `hide System Bars` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun hideSystemBars() {
        nativePlayerWindowController.hideSystemBars()
    }

    companion object {
        private const val VIDEO_LOG_TAG = "VideoBrowserVideo"
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
