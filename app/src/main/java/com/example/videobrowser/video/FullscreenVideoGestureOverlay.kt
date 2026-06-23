package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 FullscreenVideoGestureOverlay 可以拆开理解为“Fullscreen Video Gesture Overlay”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
import android.app.Activity
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.example.videobrowser.utils.DensityPixelConverter

/**
 * 全屏视频上方的透明手势层。
 *
 * 它自己不播放视频，只负责把触摸行为翻译成回调：
 * - 左右滑动：预览并提交进度跳转。
 * - 左侧上下滑动：调亮度。
 * - 右侧上下滑动：调音量。
 * - 点击/双击/长按：唤醒控件、播放暂停、快退快进。
 * - 顶部按钮：退出、锁定、倍速、字幕、队列、缩放、旋转等。
 */
class FullscreenVideoGestureOverlay(
    private val activity: Activity
) : FrameLayout(activity) {
    data class SeekPosition(
        val positionMs: Long? = null,
        val durationMs: Long? = null
    )

    var onSeekBy: ((Long) -> Unit)? = null
    var onSeekTo: ((Long) -> Unit)? = null
    var onSeekPreviewStart: (() -> SeekPosition?)? = null
    var onTogglePlayPause: (() -> Boolean?)? = null
    var onPlaybackSpeedSelected: ((Float) -> Unit)? = null
    var onDirectionalLongPressStart: ((Int) -> Unit)? = null
    var onDirectionalLongPressEnd: (() -> Unit)? = null
    var onToggleOrientation: (() -> Boolean)? = null
    var onUserInteraction: (() -> Unit)? = null
    var arePlaybackControlsVisible: (() -> Boolean)? = null
    var onExitFullscreen: (() -> Unit)? = null
    var onTrackSelectionRequested: (() -> Unit)? = null
    var onPlaybackQueueRequested: (() -> Unit)? = null
    var onVideoZoomRequested: (() -> VideoZoomMode)? = null
    var onPreviousMediaRequested: (() -> Unit)? = null
    var onNextMediaRequested: (() -> Unit)? = null
    var onRepeatModeRequested: (() -> PlaybackRepeatMode)? = null

    private val feedbackHandler = Handler(Looper.getMainLooper())
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val swipeStartDistance by lazy { maxOf(dp(MIN_SWIPE_DISTANCE_DP), touchSlop) }
    private val speedOptions = VideoSpeedOptions.menuSpeeds()

    private val exitButton = ImageButton(context)
    private val exitButtonController = FullscreenVideoExitButtonController(
        context = context,
        exitButton = exitButton,
        dp = ::dp,
        notifyUserInteraction = ::notifyUserInteraction,
        exitFullscreen = { onExitFullscreen?.invoke() }
    )
    private val lockButton = controlTextView()
    private val previousButton = controlTextView()
    private val nextButton = controlTextView()
    private val repeatButton = controlTextView()
    private val queueButton = controlTextView()
    private val speedButton = controlTextView()
    private val trackButton = controlTextView()
    private val zoomButton = controlTextView()
    private val rotateButton = controlTextView()
    private val controlsGroup = LinearLayout(context)
    private val controlHitTester = FullscreenVideoControlHitTester(
        exitButton = exitButton,
        lockButton = lockButton,
        controlsGroup = controlsGroup,
        isLocked = { locked }
    )
    private val controlsGroupController = FullscreenVideoControlsGroupController(
        context = context,
        controlsGroup = controlsGroup,
        previousButton = previousButton,
        nextButton = nextButton,
        repeatButton = repeatButton,
        queueButton = queueButton,
        speedButton = speedButton,
        trackButton = trackButton,
        zoomButton = zoomButton,
        rotateButton = rotateButton,
        dp = ::dp,
        isLocked = { locked },
        notifyUserInteraction = ::notifyUserInteraction,
        showSpeedPopup = ::showSpeedPopup,
        requestPreviousMedia = { onPreviousMediaRequested?.invoke() },
        requestNextMedia = { onNextMediaRequested?.invoke() },
        requestRepeatMode = { onRepeatModeRequested?.invoke() },
        requestPlaybackQueue = { onPlaybackQueueRequested?.invoke() },
        requestTrackSelection = { onTrackSelectionRequested?.invoke() },
        requestVideoZoomMode = { onVideoZoomRequested?.invoke() },
        requestOrientation = { onToggleOrientation?.invoke() },
        showFeedback = { text -> showFeedback(text) }
    )
    private val feedbackController = FullscreenVideoFeedbackController(
        context = context,
        feedbackHandler = feedbackHandler,
        dp = ::dp
    )
    private val seekGestureController = FullscreenVideoSeekGestureController(
        seekPreviewStart = { onSeekPreviewStart?.invoke() },
        seekBy = { offsetMs -> onSeekBy?.invoke(offsetMs) },
        seekTo = { positionMs -> onSeekTo?.invoke(positionMs) },
        currentFeedbackText = { feedbackController.currentText() },
        showFeedback = ::showFeedback,
        hideFeedback = { feedbackController.hide() }
    )
    private val touchSession = FullscreenVideoTouchSessionState()
    private val speedPopupController = FullscreenVideoSpeedPopupController(
        context = context,
        anchorView = speedButton,
        speedOptions = speedOptions,
        dp = ::dp,
        currentPlaybackSpeed = controlsGroupController::currentPlaybackSpeed,
        notifyUserInteraction = ::notifyUserInteraction,
        setPlaybackSpeed = ::setPlaybackSpeed,
        onPlaybackSpeedSelected = { speed -> onPlaybackSpeedSelected?.invoke(speed) },
        showFeedback = ::showFeedback
    )
    private val lockUiController = FullscreenVideoLockUiController(
        context = context,
        lockButton = lockButton,
        controlsGroup = controlsGroup,
        dismissSpeedPopup = speedPopupController::dismiss,
        clearGestureStateWhenLocked = ::clearGestureStateWhenLocked,
        showFeedback = ::showFeedback
    )
    private val systemGestureController = FullscreenVideoSystemGestureController(
        activity = activity,
        showFeedback = ::showFeedback
    )
    private val sideTapSeekController = FullscreenVideoSideTapSeekController(
        feedbackHandler = feedbackHandler,
        seekBy = { offsetMs -> onSeekBy?.invoke(offsetMs) },
        showFeedback = { text -> showFeedback(text) }
    )
    private val longPressController = FullscreenVideoLongPressController(
        feedbackHandler = feedbackHandler,
        touchSession = touchSession,
        isLocked = { locked },
        clearSideTapState = sideTapSeekController::clearAll,
        requestDirectionalLongPressStart = { direction -> onDirectionalLongPressStart?.invoke(direction) },
        requestDirectionalLongPressEnd = { onDirectionalLongPressEnd?.invoke() },
        showFeedback = ::showFeedback,
        hideFeedback = { feedbackController.hide() }
    )
    private val gestureEventHandler = FullscreenVideoGestureEventHandler(
        touchSession = touchSession,
        systemGestureController = systemGestureController,
        sideTapSeekController = sideTapSeekController,
        seekGestureController = seekGestureController,
        longPressController = longPressController,
        touchSlop = touchSlop,
        swipeStartDistance = { swipeStartDistance },
        viewWidth = { width },
        viewHeight = { height },
        screenZoneFor = ::screenZoneFor,
        handleTap = ::handleTap,
        resetTouchState = ::resetTouchState
    )
    private val locked: Boolean
        get() = lockUiController.locked

    init {
        visibility = View.GONE
        isClickable = true
        isFocusable = false
        setBackgroundColor(Color.TRANSPARENT)

        exitButtonController.attachTo(this)
        setupLockButton()
        controlsGroupController.attachTo(this)
        setupFeedbackView()
        lockUiController.update()
        setPlaybackSpeed(FullscreenVideoControlsGroupController.DEFAULT_PLAYBACK_SPEED)
        setLandscape(true)
    }

    /**
     * 函数 `showOverlay`：控制 `show Overlay` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun showOverlay() {
        if (visibility != View.VISIBLE) {
            systemGestureController.captureWindowBrightness()
        }
        visibility = View.VISIBLE
        bringToFront()
        lockUiController.setLocked(false, announce = false)
    }

    /**
     * 函数 `hideOverlay`：控制 `hide Overlay` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun hideOverlay() {
        speedPopupController.dismiss()
        longPressController.cancelScheduled()
        longPressController.stopActive()
        systemGestureController.restoreWindowBrightness()
        lockUiController.setLocked(false, announce = false)
        resetTouchState()
        sideTapSeekController.clearAll()
        feedbackController.hide()
        visibility = View.GONE
    }

    /**
     * 函数 `setPlaybackSpeed`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param speed 参数类型为 `Float`，表示函数执行 `speed` 相关逻辑时需要读取或处理的输入。
     */
    fun setPlaybackSpeed(speed: Float) {
        controlsGroupController.setPlaybackSpeed(speed)
    }

    /**
     * 函数 `setLandscape`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param isLandscape 参数类型为 `Boolean`，表示函数执行 `isLandscape` 相关逻辑时需要读取或处理的输入。
     */
    fun setLandscape(isLandscape: Boolean) {
        controlsGroupController.setLandscape(isLandscape)
    }

    /**
     * 函数 `setQueueControlsVisible`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param visible 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setQueueControlsVisible(visible: Boolean) {
        controlsGroupController.setQueueControlsVisible(visible)
    }

    /**
     * 函数 `setRepeatMode`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param mode 参数类型为 `PlaybackRepeatMode`，表示函数执行 `mode` 相关逻辑时需要读取或处理的输入。
     */
    fun setRepeatMode(mode: PlaybackRepeatMode) {
        controlsGroupController.setRepeatMode(mode)
    }

    /**
     * 函数 `setVideoZoomMode`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param mode 参数类型为 `VideoZoomMode`，表示函数执行 `mode` 相关逻辑时需要读取或处理的输入。
     */
    fun setVideoZoomMode(mode: VideoZoomMode) {
        controlsGroupController.setVideoZoomMode(mode)
    }

    /**
     * 函数 `dispatchTouchEvent`：封装 `dispatch Touch Event` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param event 参数类型为 `MotionEvent`，表示函数执行 `event` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // 入口先处理“控件点击”和“底部系统播放器控件透传”，剩下的触摸才进入手势识别。
        if (visibility != View.VISIBLE || width <= 0 || height <= 0) {
            return false
        }

        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            touchSession.beginDispatchDown(
                playbackControlsVisible = arePlaybackControlsVisible?.invoke() ?: true,
                startedOnControl = controlHitTester.isControlPoint(event.x, event.y),
                startedInBottomPassthrough =
                    !locked && event.y >= height - bottomPassthroughHeight()
            )
        }

        if (touchSession.touchStartedOnControl) {
            super.dispatchTouchEvent(event)
            if (FullscreenVideoMotionEvents.isFinishedAction(event)) {
                touchSession.clearControlStart()
            }
            return true
        }

        if (touchSession.touchStartedInBottomPassthrough) {
            if (FullscreenVideoMotionEvents.isFinishedAction(event)) {
                touchSession.clearBottomPassthroughStart()
            }
            return false
        }

        if (FullscreenVideoMotionEvents.isWakeControlsAction(event)) {
            notifyUserInteraction()
        }

        if (locked) {
            // 锁定后只吞掉触摸并保留解锁按钮，避免误触改变播放状态。
            if (FullscreenVideoMotionEvents.isFinishedAction(event)) {
                resetTouchState()
            }
            return true
        }

        return gestureEventHandler.handle(event)
    }

    /**
     * 函数 `dispatchHoverEvent`：封装 `dispatch Hover Event` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param event 参数类型为 `MotionEvent`，表示函数执行 `event` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        if (visibility == View.VISIBLE && FullscreenVideoMotionEvents.isWakeControlsAction(event)) {
            notifyUserInteraction()
        }
        return super.dispatchHoverEvent(event)
    }

    /**
     * 函数 `onDetachedFromWindow`：处理 `on Detached From Window` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    override fun onDetachedFromWindow() {
        hideOverlay()
        super.onDetachedFromWindow()
    }

    /**
     * 函数 `setupLockButton`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupLockButton() {
        lockButton.setOnClickListener {
            notifyUserInteraction()
            lockUiController.toggle(announce = true)
        }
        addView(
            lockButton,
            LayoutParams(
                dp(44),
                dp(44)
            ).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                leftMargin = dp(14)
            }
        )
    }

    /**
     * 函数 `setupFeedbackView`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupFeedbackView() {
        addView(
            feedbackController.view,
            LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                leftMargin = dp(24)
                rightMargin = dp(24)
            }
        )
    }

    /**
     * 函数 `controlTextView`：封装 `control Text View` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun controlTextView(): TextView {
        return FullscreenVideoControlsGroupController.controlTextView(context, ::dp)
    }

    /**
     * 函数 `handleTap`：处理 `handle Tap` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param upX 参数类型为 `Float`，表示函数执行 `upX` 相关逻辑时需要读取或处理的输入。
     * @param eventTime 参数类型为 `Long`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    private fun handleTap(upX: Float, eventTime: Long) {
        notifyUserInteraction()
        when (val zone = screenZoneFor(upX)) {
            VideoGestureScreenZone.CENTER -> {
                sideTapSeekController.clearPendingTap()
                if (touchSession.playbackControlsVisibleOnTouchStart) {
                    onTogglePlayPause?.invoke()
                }
            }
            VideoGestureScreenZone.LEFT,
            VideoGestureScreenZone.RIGHT -> sideTapSeekController.registerTap(zone, eventTime)
            VideoGestureScreenZone.NONE -> Unit
        }
    }

    /**
     * 函数 `showSpeedPopup`：控制 `show Speed Popup` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showSpeedPopup() {
        speedPopupController.show()
    }

    private fun clearGestureStateWhenLocked() {
        longPressController.stopActive()
        gestureEventHandler.cancelActiveHorizontalSeek()
        sideTapSeekController.clearAll()
    }

    /**
     * 函数 `showFeedback`：控制 `show Feedback` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
     * @param autoHide 参数类型为 `Boolean`，表示函数执行 `autoHide` 相关逻辑时需要读取或处理的输入。
     */
    private fun showFeedback(text: String, autoHide: Boolean = true) {
        feedbackController.show(text, autoHide)
    }

    /**
     * 函数 `notifyUserInteraction`：封装 `notify User Interaction` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun notifyUserInteraction() {
        onUserInteraction?.invoke()
    }

    /**
     * 函数 `resetTouchState`：封装 `reset Touch State` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun resetTouchState() {
        touchSession.reset()
    }

    /**
     * 函数 `screenZoneFor`：封装 `screen Zone For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param x 参数类型为 `Float`，表示函数执行 `x` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun screenZoneFor(x: Float): VideoGestureScreenZone {
        return FullscreenVideoGestureMath.screenZoneFor(x, width)
    }

    /**
     * 函数 `bottomPassthroughHeight`：封装 `bottom Passthrough Height` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun bottomPassthroughHeight(): Int {
        return dp(BOTTOM_PASSTHROUGH_DP)
    }

    /**
     * 函数 `dp`：封装 `dp` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun dp(value: Int): Int {
        return DensityPixelConverter.roundDp(value, resources)
    }

    private companion object {
        private const val BOTTOM_PASSTHROUGH_DP = 92
        private const val MIN_SWIPE_DISTANCE_DP = 10
    }
}
