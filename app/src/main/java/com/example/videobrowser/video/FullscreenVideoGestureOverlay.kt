package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 FullscreenVideoGestureOverlay 可以拆开理解为“Fullscreen Video Gesture Overlay”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.example.videobrowser.R
import com.example.videobrowser.utils.BrowserDrawableFactory
import com.example.videobrowser.utils.DensityPixelConverter
import kotlin.math.abs

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

    private val audioManager =
        activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val feedbackHandler = Handler(Looper.getMainLooper())
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val swipeStartDistance by lazy { maxOf(dp(MIN_SWIPE_DISTANCE_DP), touchSlop) }
    private val speedOptions = VideoSpeedOptions.menuSpeeds()

    private val exitButton = ImageButton(context)
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
    private val feedbackView = TextView(context)

    private var locked = false
    private var landscape = true
    private var playbackSpeed = DEFAULT_PLAYBACK_SPEED
    private var repeatMode = PlaybackRepeatMode.NONE
    private var videoZoomMode = VideoZoomMode.FIT
    private var savedWindowBrightness: Float? = null
    private var touchStartedOnControl = false
    private var touchStartedInBottomPassthrough = false
    private var playbackControlsVisibleOnTouchStart = true
    private var activeGesture = VerticalGesture.NONE
    private var tapCandidate = false
    private var longPressActive = false
    private var downZone = VideoGestureScreenZone.CENTER
    private var seekStartPositionMs: Long? = null
    private var seekDurationMs: Long? = null
    private var pendingHorizontalSeekMs = 0L
    private var pendingSeekTargetMs: Long? = null
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L
    private var initialBrightness = DEFAULT_BRIGHTNESS
    private var initialVolume = 0
    private var pendingTapZone = VideoGestureScreenZone.NONE
    private var pendingTapTime = 0L
    private var seekAccumulatorDirection = 0
    private var seekAccumulatorCount = 0
    private val speedPopupController = FullscreenVideoSpeedPopupController(
        context = context,
        anchorView = speedButton,
        speedOptions = speedOptions,
        dp = ::dp,
        currentPlaybackSpeed = { playbackSpeed },
        notifyUserInteraction = ::notifyUserInteraction,
        setPlaybackSpeed = ::setPlaybackSpeed,
        onPlaybackSpeedSelected = { speed -> onPlaybackSpeedSelected?.invoke(speed) },
        showFeedback = ::showFeedback
    )

    private val hideFeedbackRunnable = Runnable {
        feedbackView.visibility = View.GONE
    }

    private val clearPendingTapRunnable = Runnable {
        pendingTapZone = VideoGestureScreenZone.NONE
        pendingTapTime = 0L
    }

    private val clearSeekAccumulatorRunnable = Runnable {
        seekAccumulatorDirection = 0
        seekAccumulatorCount = 0
    }

    private val longPressRunnable = Runnable {
        triggerLongPress()
    }

    init {
        visibility = View.GONE
        isClickable = true
        isFocusable = false
        setBackgroundColor(Color.TRANSPARENT)

        setupExitButton()
        setupLockButton()
        setupControlsGroup()
        setupFeedbackView()
        updateLockUi()
        setPlaybackSpeed(DEFAULT_PLAYBACK_SPEED)
        setLandscape(true)
    }

    /**
     * 函数 `showOverlay`：控制 `show Overlay` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun showOverlay() {
        if (visibility != View.VISIBLE) {
            savedWindowBrightness = activity.window.attributes.screenBrightness
        }
        visibility = View.VISIBLE
        bringToFront()
        setLocked(false, announce = false)
    }

    /**
     * 函数 `hideOverlay`：控制 `hide Overlay` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun hideOverlay() {
        speedPopupController.dismiss()
        stopLongPress()
        restoreWindowBrightness()
        setLocked(false, announce = false)
        resetTouchState()
        feedbackHandler.removeCallbacks(hideFeedbackRunnable)
        feedbackHandler.removeCallbacks(clearPendingTapRunnable)
        feedbackHandler.removeCallbacks(clearSeekAccumulatorRunnable)
        feedbackHandler.removeCallbacks(longPressRunnable)
        feedbackView.visibility = View.GONE
        visibility = View.GONE
    }

    /**
     * 函数 `setPlaybackSpeed`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param speed 参数类型为 `Float`，表示函数执行 `speed` 相关逻辑时需要读取或处理的输入。
     */
    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = FullscreenVideoGestureMath.normalizeSpeed(speed, DEFAULT_PLAYBACK_SPEED)
        speedButton.text = VideoGestureFeedbackFormatter.formatSpeed(playbackSpeed)
        speedButton.contentDescription = context.getString(
            R.string.video_control_speed,
            VideoGestureFeedbackFormatter.formatSpeed(playbackSpeed)
        )
    }

    /**
     * 函数 `setLandscape`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param isLandscape 参数类型为 `Boolean`，表示函数执行 `isLandscape` 相关逻辑时需要读取或处理的输入。
     */
    fun setLandscape(isLandscape: Boolean) {
        landscape = isLandscape
        val label = if (landscape) {
            context.getString(R.string.video_control_rotate_to_portrait)
        } else {
            context.getString(R.string.video_control_rotate_to_landscape)
        }
        rotateButton.text = ROTATE_ICON
        rotateButton.contentDescription = label
        ViewCompat.setTooltipText(rotateButton, label)
    }

    /**
     * 函数 `setQueueControlsVisible`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param visible 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     */
    fun setQueueControlsVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        previousButton.visibility = visibility
        nextButton.visibility = visibility
        repeatButton.visibility = visibility
        queueButton.visibility = visibility
    }

    /**
     * 函数 `setRepeatMode`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param mode 参数类型为 `PlaybackRepeatMode`，表示函数执行 `mode` 相关逻辑时需要读取或处理的输入。
     */
    fun setRepeatMode(mode: PlaybackRepeatMode) {
        repeatMode = mode
        repeatButton.text = when (mode) {
            PlaybackRepeatMode.NONE -> REPEAT_NONE_ICON
            PlaybackRepeatMode.ONE -> REPEAT_ONE_ICON
            PlaybackRepeatMode.ALL -> REPEAT_ALL_ICON
        }
        val label = when (mode) {
            PlaybackRepeatMode.NONE -> context.getString(R.string.video_control_repeat_none)
            PlaybackRepeatMode.ONE -> context.getString(R.string.video_control_repeat_one)
            PlaybackRepeatMode.ALL -> context.getString(R.string.video_control_repeat_all)
        }
        repeatButton.contentDescription = label
        ViewCompat.setTooltipText(repeatButton, label)
    }

    /**
     * 函数 `setVideoZoomMode`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param mode 参数类型为 `VideoZoomMode`，表示函数执行 `mode` 相关逻辑时需要读取或处理的输入。
     */
    fun setVideoZoomMode(mode: VideoZoomMode) {
        videoZoomMode = mode
        zoomButton.text = when (mode) {
            VideoZoomMode.FIT -> ZOOM_FIT_ICON
            VideoZoomMode.STRETCH -> ZOOM_STRETCH_ICON
            VideoZoomMode.CROP -> ZOOM_CROP_ICON
        }
        val label = when (mode) {
            VideoZoomMode.FIT -> context.getString(R.string.video_control_zoom_fit)
            VideoZoomMode.STRETCH -> context.getString(R.string.video_control_zoom_stretch)
            VideoZoomMode.CROP -> context.getString(R.string.video_control_zoom_crop)
        }
        zoomButton.contentDescription = label
        ViewCompat.setTooltipText(zoomButton, label)
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
            playbackControlsVisibleOnTouchStart = arePlaybackControlsVisible?.invoke() ?: true
            touchStartedOnControl = isControlPoint(event.x, event.y)
            touchStartedInBottomPassthrough =
                !locked && event.y >= height - bottomPassthroughHeight()
        }

        if (touchStartedOnControl) {
            super.dispatchTouchEvent(event)
            if (event.isFinishedAction()) {
                touchStartedOnControl = false
            }
            return true
        }

        if (touchStartedInBottomPassthrough) {
            if (event.isFinishedAction()) {
                touchStartedInBottomPassthrough = false
            }
            return false
        }

        if (event.isWakeControlsAction()) {
            notifyUserInteraction()
        }

        if (locked) {
            // 锁定后只吞掉触摸并保留解锁按钮，避免误触改变播放状态。
            if (event.isFinishedAction()) {
                resetTouchState()
            }
            return true
        }

        return handleGestureEvent(event)
    }

    /**
     * 函数 `dispatchHoverEvent`：封装 `dispatch Hover Event` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param event 参数类型为 `MotionEvent`，表示函数执行 `event` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        if (visibility == View.VISIBLE && event.isWakeControlsAction()) {
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
     * 函数 `setupExitButton`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupExitButton() {
        val label = context.getString(R.string.video_control_exit_fullscreen)
        exitButton.apply {
            background = BrowserDrawableFactory.roundedBackground(
                Color.argb(178, 0, 0, 0),
                dp(20)
            )
            contentDescription = label
            isClickable = true
            isFocusable = true
            scaleType = ImageView.ScaleType.CENTER
            setColorFilter(Color.WHITE)
            setImageResource(R.drawable.ic_close_24)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setOnClickListener {
                notifyUserInteraction()
                onExitFullscreen?.invoke()
            }
        }
        ViewCompat.setTooltipText(exitButton, label)
        addView(
            exitButton,
            LayoutParams(
                dp(44),
                dp(44)
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                topMargin = dp(14)
                leftMargin = dp(14)
            }
        )
    }

    /**
     * 函数 `setupLockButton`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupLockButton() {
        lockButton.setOnClickListener {
            notifyUserInteraction()
            setLocked(!locked, announce = true)
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
     * 函数 `setupControlsGroup`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupControlsGroup() {
        controlsGroup.orientation = LinearLayout.HORIZONTAL
        controlsGroup.gravity = Gravity.CENTER_VERTICAL
        controlsGroup.addView(
            previousButton,
            LinearLayout.LayoutParams(
                dp(44),
                dp(40)
            ).apply {
                marginEnd = dp(8)
            }
        )
        controlsGroup.addView(
            nextButton,
            LinearLayout.LayoutParams(
                dp(44),
                dp(40)
            ).apply {
                marginEnd = dp(8)
            }
        )
        controlsGroup.addView(
            repeatButton,
            LinearLayout.LayoutParams(
                dp(44),
                dp(40)
            ).apply {
                marginEnd = dp(8)
            }
        )
        controlsGroup.addView(
            queueButton,
            LinearLayout.LayoutParams(
                dp(44),
                dp(40)
            ).apply {
                marginEnd = dp(8)
            }
        )
        controlsGroup.addView(
            speedButton,
            LinearLayout.LayoutParams(
                dp(62),
                dp(40)
            ).apply {
                marginEnd = dp(8)
            }
        )
        controlsGroup.addView(
            trackButton,
            LinearLayout.LayoutParams(
                dp(44),
                dp(40)
            ).apply {
                marginEnd = dp(8)
            }
        )
        controlsGroup.addView(
            zoomButton,
            LinearLayout.LayoutParams(
                dp(44),
                dp(40)
            ).apply {
                marginEnd = dp(8)
            }
        )
        controlsGroup.addView(
            rotateButton,
            LinearLayout.LayoutParams(
                dp(44),
                dp(40)
            )
        )

        speedButton.setOnClickListener {
            notifyUserInteraction()
            if (!locked) showSpeedPopup()
        }
        previousButton.text = PREVIOUS_ICON
        previousButton.contentDescription = context.getString(R.string.video_control_previous)
        ViewCompat.setTooltipText(
            previousButton,
            context.getString(R.string.video_control_previous)
        )
        previousButton.setOnClickListener {
            notifyUserInteraction()
            if (locked) return@setOnClickListener
            onPreviousMediaRequested?.invoke()
        }
        nextButton.text = NEXT_ICON
        nextButton.contentDescription = context.getString(R.string.video_control_next)
        ViewCompat.setTooltipText(nextButton, context.getString(R.string.video_control_next))
        nextButton.setOnClickListener {
            notifyUserInteraction()
            if (locked) return@setOnClickListener
            onNextMediaRequested?.invoke()
        }
        repeatButton.setOnClickListener {
            notifyUserInteraction()
            if (locked) return@setOnClickListener
            val mode = onRepeatModeRequested?.invoke() ?: repeatMode
            setRepeatMode(mode)
            showFeedback(repeatButton.contentDescription?.toString().orEmpty())
        }
        val queueLabel = context.getString(R.string.video_control_queue)
        queueButton.text = QUEUE_ICON
        queueButton.contentDescription = queueLabel
        ViewCompat.setTooltipText(queueButton, queueLabel)
        queueButton.setOnClickListener {
            notifyUserInteraction()
            if (locked) return@setOnClickListener
            onPlaybackQueueRequested?.invoke()
        }
        setQueueControlsVisible(false)
        setRepeatMode(PlaybackRepeatMode.NONE)
        val trackLabel = context.getString(R.string.video_control_tracks)
        trackButton.text = TRACK_ICON
        trackButton.contentDescription = trackLabel
        ViewCompat.setTooltipText(trackButton, trackLabel)
        trackButton.setOnClickListener {
            notifyUserInteraction()
            if (locked) return@setOnClickListener
            onTrackSelectionRequested?.invoke()
        }
        setVideoZoomMode(VideoZoomMode.FIT)
        zoomButton.setOnClickListener {
            notifyUserInteraction()
            if (locked) return@setOnClickListener
            val mode = onVideoZoomRequested?.invoke() ?: videoZoomMode.next()
            setVideoZoomMode(mode)
            showFeedback(zoomButton.contentDescription?.toString().orEmpty())
        }
        rotateButton.setOnClickListener {
            notifyUserInteraction()
            if (locked) return@setOnClickListener
            val isLandscape = onToggleOrientation?.invoke() ?: !landscape
            setLandscape(isLandscape)
            showFeedback(ROTATE_ICON)
        }

        addView(
            controlsGroup,
            LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = dp(14)
                rightMargin = dp(14)
            }
        )
    }

    /**
     * 函数 `setupFeedbackView`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun setupFeedbackView() {
        feedbackView.apply {
            visibility = View.GONE
            gravity = Gravity.CENTER
            includeFontPadding = false
            minHeight = dp(46)
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            textSize = 18f
            setPadding(dp(20), dp(8), dp(20), dp(8))
            background = BrowserDrawableFactory.roundedBackground(
                Color.argb(196, 0, 0, 0),
                dp(20)
            )
        }
        addView(
            feedbackView,
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
        return TextView(context).apply {
            gravity = Gravity.CENTER
            includeFontPadding = false
            isClickable = true
            isFocusable = true
            minWidth = dp(44)
            setPadding(dp(8), 0, dp(8), 0)
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            textSize = 18f
            background = BrowserDrawableFactory.roundedBackground(
                Color.argb(178, 0, 0, 0),
                dp(20)
            )
        }
    }

    /**
     * 函数 `handleGestureEvent`：处理 `handle Gesture Event` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param event 参数类型为 `MotionEvent`，表示函数执行 `event` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun handleGestureEvent(event: MotionEvent): Boolean {
        // 这里是触摸事件的总入口：先记录起点区域，再根据移动方向决定是哪一种手势。
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downTime = event.eventTime
                downZone = screenZoneFor(downX)
                initialBrightness = currentWindowBrightness()
                initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                activeGesture = VerticalGesture.NONE
                tapCandidate = true
                longPressActive = false
                if (downZone.isSide()) {
                    feedbackHandler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT_MS)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - downX
                val deltaY = event.y - downY
                if (!longPressActive &&
                    (abs(deltaX) > touchSlop || abs(deltaY) > touchSlop)
                ) {
                    tapCandidate = false
                    feedbackHandler.removeCallbacks(longPressRunnable)
                }
                if (!longPressActive &&
                    activeGesture == VerticalGesture.NONE &&
                    abs(deltaX) >= swipeStartDistance &&
                    abs(deltaX) > abs(deltaY)
                ) {
                    beginHorizontalSeek(deltaX)
                } else if (!longPressActive &&
                    activeGesture == VerticalGesture.NONE &&
                    downZone.isSide() &&
                    abs(deltaY) >= swipeStartDistance &&
                    abs(deltaY) > abs(deltaX) * VERTICAL_GESTURE_RATIO
                ) {
                    activeGesture = if (downZone == VideoGestureScreenZone.LEFT) {
                        VerticalGesture.BRIGHTNESS
                    } else {
                        VerticalGesture.VOLUME
                    }
                }
                when (activeGesture) {
                    VerticalGesture.HORIZONTAL_SEEK -> updateHorizontalSeek(deltaX)
                    VerticalGesture.BRIGHTNESS -> updateBrightness(deltaY)
                    VerticalGesture.VOLUME -> updateVolume(deltaY)
                    VerticalGesture.NONE -> Unit
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                feedbackHandler.removeCallbacks(longPressRunnable)
                if (longPressActive) {
                    stopLongPress()
                    resetTouchState()
                    return true
                }
                if (activeGesture == VerticalGesture.HORIZONTAL_SEEK) {
                    finishHorizontalSeek(commit = true)
                    resetTouchState()
                    return true
                }
                if (activeGesture == VerticalGesture.NONE &&
                    tapCandidate &&
                    event.eventTime - downTime <= TAP_MAX_DURATION_MS
                ) {
                    handleTap(event.x, event.eventTime)
                }
                resetTouchState()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                feedbackHandler.removeCallbacks(longPressRunnable)
                stopLongPress()
                if (activeGesture == VerticalGesture.HORIZONTAL_SEEK) {
                    finishHorizontalSeek(commit = false)
                }
                resetTouchState()
                return true
            }
        }
        return true
    }

    /**
     * 函数 `beginHorizontalSeek`：封装 `begin Horizontal Seek` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param deltaX 参数类型为 `Float`，表示函数执行 `deltaX` 相关逻辑时需要读取或处理的输入。
     */
    private fun beginHorizontalSeek(deltaX: Float) {
        // 横向滑动开始时记录当前播放位置，后续移动只是在这个基础上计算预览目标。
        activeGesture = VerticalGesture.HORIZONTAL_SEEK
        clearPendingSideTap()
        clearSeekAccumulator()
        feedbackHandler.removeCallbacks(longPressRunnable)

        val position = onSeekPreviewStart?.invoke()
        seekStartPositionMs = position?.positionMs?.takeIf { it >= 0L }
        seekDurationMs = position?.durationMs?.takeIf { it > 0L }
        pendingHorizontalSeekMs = 0L
        pendingSeekTargetMs = seekStartPositionMs
        updateHorizontalSeek(deltaX)
    }

    /**
     * 函数 `updateHorizontalSeek`：根据最新状态刷新 `update Horizontal Seek` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param deltaX 参数类型为 `Float`，表示函数执行 `deltaX` 相关逻辑时需要读取或处理的输入。
     */
    private fun updateHorizontalSeek(deltaX: Float) {
        val offsetMs = VideoSeekDragCalculator.offsetForDrag(deltaX, width, seekDurationMs)
        pendingHorizontalSeekMs = offsetMs

        val start = seekStartPositionMs
        val duration = seekDurationMs
        val target = start?.let {
            VideoSeekDragCalculator.targetForDrag(
                startPositionMs = it,
                durationMs = duration,
                deltaX = deltaX,
                viewWidth = width
            )
        }
        pendingSeekTargetMs = target

        showFeedback(
            VideoGestureFeedbackFormatter.formatSeekPreview(offsetMs, target, duration),
            autoHide = false
        )
    }

    /**
     * 函数 `finishHorizontalSeek`：封装 `finish Horizontal Seek` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param commit 参数类型为 `Boolean`，表示函数执行 `commit` 相关逻辑时需要读取或处理的输入。
     */
    private fun finishHorizontalSeek(commit: Boolean) {
        // 抬手时才真正 seek，移动过程只显示反馈，避免每一帧都让播放器跳转。
        val feedbackText = feedbackView.text?.toString().orEmpty()
        if (commit && pendingHorizontalSeekMs != 0L) {
            pendingSeekTargetMs?.let { onSeekTo?.invoke(it) }
                ?: onSeekBy?.invoke(pendingHorizontalSeekMs)
        }

        if (commit && feedbackText.isNotBlank()) {
            showFeedback(feedbackText)
        } else {
            feedbackHandler.removeCallbacks(hideFeedbackRunnable)
            feedbackView.visibility = View.GONE
        }

        seekStartPositionMs = null
        seekDurationMs = null
        pendingHorizontalSeekMs = 0L
        pendingSeekTargetMs = null
    }

    /**
     * 函数 `updateBrightness`：根据最新状态刷新 `update Brightness` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param deltaY 参数类型为 `Float`，表示函数执行 `deltaY` 相关逻辑时需要读取或处理的输入。
     */
    private fun updateBrightness(deltaY: Float) {
        val brightness = FullscreenVideoGestureMath.brightnessForDrag(
            initialBrightness = initialBrightness,
            deltaY = deltaY,
            viewHeight = height
        )
        val attributes = activity.window.attributes
        attributes.screenBrightness = brightness
        activity.window.attributes = attributes
        showFeedback(VideoGestureFeedbackFormatter.formatBrightness(brightness))
    }

    /**
     * 函数 `updateVolume`：根据最新状态刷新 `update Volume` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param deltaY 参数类型为 `Float`，表示函数执行 `deltaY` 相关逻辑时需要读取或处理的输入。
     */
    private fun updateVolume(deltaY: Float) {
        val minVolume = streamMinVolume()
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val nextVolume = FullscreenVideoGestureMath.volumeForDrag(
            initialVolume = initialVolume,
            deltaY = deltaY,
            viewHeight = height,
            minVolume = minVolume,
            maxVolume = maxVolume
        ) ?: return
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nextVolume, 0)
        showFeedback(VideoGestureFeedbackFormatter.formatVolume(nextVolume, minVolume, maxVolume))
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
                clearPendingSideTap()
                if (playbackControlsVisibleOnTouchStart) {
                    onTogglePlayPause?.invoke()
                }
            }
            VideoGestureScreenZone.LEFT,
            VideoGestureScreenZone.RIGHT -> registerSideTap(zone, eventTime)
            VideoGestureScreenZone.NONE -> Unit
        }
    }

    /**
     * 函数 `registerSideTap`：封装 `register Side Tap` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param zone 参数类型为 `VideoGestureScreenZone`，表示函数执行 `zone` 相关逻辑时需要读取或处理的输入。
     * @param eventTime 参数类型为 `Long`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     */
    private fun registerSideTap(zone: VideoGestureScreenZone, eventTime: Long) {
        if (pendingTapZone == zone && eventTime - pendingTapTime <= DOUBLE_TAP_TIMEOUT_MS) {
            clearPendingSideTap()
            handleDoubleTap(zone)
            return
        }
        pendingTapZone = zone
        pendingTapTime = eventTime
        feedbackHandler.removeCallbacks(clearPendingTapRunnable)
        feedbackHandler.postDelayed(clearPendingTapRunnable, DOUBLE_TAP_TIMEOUT_MS)
    }

    /**
     * 函数 `handleDoubleTap`：处理 `handle Double Tap` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param zone 参数类型为 `VideoGestureScreenZone`，表示函数执行 `zone` 相关逻辑时需要读取或处理的输入。
     */
    private fun handleDoubleTap(zone: VideoGestureScreenZone) {
        val direction = if (zone == VideoGestureScreenZone.LEFT) -1 else 1
        onSeekBy?.invoke(direction * SEEK_STEP_MS)
        if (seekAccumulatorDirection != direction) {
            seekAccumulatorDirection = direction
            seekAccumulatorCount = 0
        }
        seekAccumulatorCount += 1
        val seconds = direction * seekAccumulatorCount * SEEK_STEP_SECONDS
        showFeedback(VideoGestureFeedbackFormatter.formatSeekSeconds(seconds))
        feedbackHandler.removeCallbacks(clearSeekAccumulatorRunnable)
        feedbackHandler.postDelayed(clearSeekAccumulatorRunnable, SEEK_ACCUMULATE_RESET_MS)
    }

    /**
     * 函数 `triggerLongPress`：封装 `trigger Long Press` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun triggerLongPress() {
        // 长按左右区域触发连续快退/快进；中心区域不触发，避免和普通点击冲突。
        if (locked || longPressActive || activeGesture != VerticalGesture.NONE || !downZone.isSide()) {
            return
        }
        longPressActive = true
        tapCandidate = false
        clearPendingSideTap()
        clearSeekAccumulator()
        val direction = if (downZone == VideoGestureScreenZone.LEFT) -1 else 1
        onDirectionalLongPressStart?.invoke(direction)
        showFeedback(
            VideoGestureFeedbackFormatter.formatSpeed(VideoSpeedOptions.longPressSpeed),
            autoHide = false
        )
    }

    /**
     * 函数 `stopLongPress`：封装 `stop Long Press` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun stopLongPress() {
        if (!longPressActive) return
        longPressActive = false
        onDirectionalLongPressEnd?.invoke()
        feedbackHandler.removeCallbacks(hideFeedbackRunnable)
        feedbackView.visibility = View.GONE
    }

    /**
     * 函数 `showSpeedPopup`：控制 `show Speed Popup` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun showSpeedPopup() {
        speedPopupController.show()
    }

    /**
     * 函数 `setLocked`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `Boolean`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param announce 参数类型为 `Boolean`，表示函数执行 `announce` 相关逻辑时需要读取或处理的输入。
     */
    private fun setLocked(value: Boolean, announce: Boolean) {
        if (locked == value && announce) {
            showFeedback(if (locked) LOCKED_ICON else UNLOCKED_ICON)
            return
        }
        locked = value
        if (locked) {
            stopLongPress()
            if (activeGesture == VerticalGesture.HORIZONTAL_SEEK) {
                finishHorizontalSeek(commit = false)
            }
            clearPendingSideTap()
            clearSeekAccumulator()
        }
        updateLockUi()
        if (announce) {
            showFeedback(if (locked) LOCKED_ICON else UNLOCKED_ICON)
        }
    }

    /**
     * 函数 `updateLockUi`：根据最新状态刷新 `update Lock Ui` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun updateLockUi() {
        val controlLabel = context.getString(
            if (locked) R.string.video_control_unlock else R.string.video_control_lock
        )
        lockButton.text = if (locked) UNLOCKED_ICON else LOCKED_ICON
        lockButton.contentDescription = controlLabel
        ViewCompat.setTooltipText(lockButton, controlLabel)
        controlsGroup.visibility = if (locked) View.GONE else View.VISIBLE
        if (locked) {
            speedPopupController.dismiss()
        }
    }

    /**
     * 函数 `isControlPoint`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param x 参数类型为 `Float`，表示函数执行 `x` 相关逻辑时需要读取或处理的输入。
     * @param y 参数类型为 `Float`，表示函数执行 `y` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isControlPoint(x: Float, y: Float): Boolean {
        return isPointInside(exitButton, x, y) ||
            isPointInside(lockButton, x, y) ||
            (!locked && isPointInside(controlsGroup, x, y))
    }

    /**
     * 函数 `isPointInside`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param view 参数类型为 `View`，表示当前参与操作的视图对象，函数会从中读取状态或更新界面。
     * @param x 参数类型为 `Float`，表示函数执行 `x` 相关逻辑时需要读取或处理的输入。
     * @param y 参数类型为 `Float`，表示函数执行 `y` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isPointInside(view: View, x: Float, y: Float): Boolean {
        if (view.visibility != View.VISIBLE) return false
        return x >= view.left &&
            x <= view.right &&
            y >= view.top &&
            y <= view.bottom
    }

    /**
     * 函数 `currentWindowBrightness`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun currentWindowBrightness(): Float {
        val current = activity.window.attributes.screenBrightness
        if (current >= 0f) {
            return FullscreenVideoGestureMath.clampBrightness(current)
        }
        return runCatching {
            Settings.System.getInt(
                activity.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            ) / 255f
        }.getOrDefault(DEFAULT_BRIGHTNESS).let(FullscreenVideoGestureMath::clampBrightness)
    }

    /**
     * 函数 `restoreWindowBrightness`：封装 `restore Window Brightness` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun restoreWindowBrightness() {
        val saved = savedWindowBrightness ?: return
        val attributes = activity.window.attributes
        attributes.screenBrightness = saved
        activity.window.attributes = attributes
        savedWindowBrightness = null
    }

    /**
     * 函数 `streamMinVolume`：封装 `stream Min Volume` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun streamMinVolume(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        } else {
            0
        }
    }

    /**
     * 函数 `showFeedback`：控制 `show Feedback` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param text 参数类型为 `String`，表示函数执行 `text` 相关逻辑时需要读取或处理的输入。
     * @param autoHide 参数类型为 `Boolean`，表示函数执行 `autoHide` 相关逻辑时需要读取或处理的输入。
     */
    private fun showFeedback(text: String, autoHide: Boolean = true) {
        feedbackView.text = text
        feedbackView.visibility = View.VISIBLE
        feedbackView.bringToFront()
        feedbackHandler.removeCallbacks(hideFeedbackRunnable)
        if (autoHide) {
            feedbackHandler.postDelayed(hideFeedbackRunnable, FEEDBACK_DURATION_MS)
        }
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
        activeGesture = VerticalGesture.NONE
        tapCandidate = false
        touchStartedOnControl = false
        touchStartedInBottomPassthrough = false
        playbackControlsVisibleOnTouchStart = true
        downZone = VideoGestureScreenZone.CENTER
    }

    /**
     * 函数 `clearPendingSideTap`：封装 `clear Pending Side Tap` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun clearPendingSideTap() {
        pendingTapZone = VideoGestureScreenZone.NONE
        pendingTapTime = 0L
        feedbackHandler.removeCallbacks(clearPendingTapRunnable)
    }

    /**
     * 函数 `clearSeekAccumulator`：封装 `clear Seek Accumulator` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    private fun clearSeekAccumulator() {
        seekAccumulatorDirection = 0
        seekAccumulatorCount = 0
        feedbackHandler.removeCallbacks(clearSeekAccumulatorRunnable)
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
     * 函数 `isFinishedAction`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun MotionEvent.isFinishedAction(): Boolean {
        return actionMasked == MotionEvent.ACTION_UP ||
            actionMasked == MotionEvent.ACTION_CANCEL
    }

    /**
     * 函数 `isWakeControlsAction`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun MotionEvent.isWakeControlsAction(): Boolean {
        return actionMasked == MotionEvent.ACTION_DOWN ||
            actionMasked == MotionEvent.ACTION_MOVE ||
            actionMasked == MotionEvent.ACTION_UP ||
            actionMasked == MotionEvent.ACTION_HOVER_ENTER ||
            actionMasked == MotionEvent.ACTION_HOVER_MOVE
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

    private enum class VerticalGesture {
        NONE,
        HORIZONTAL_SEEK,
        BRIGHTNESS,
        VOLUME
    }

    private companion object {
        private const val DEFAULT_PLAYBACK_SPEED = 1f
        private const val DEFAULT_BRIGHTNESS = 0.5f
        private const val SEEK_STEP_MS = 10_000L
        private const val SEEK_STEP_SECONDS = 10
        private const val TAP_MAX_DURATION_MS = 260L
        private const val DOUBLE_TAP_TIMEOUT_MS = 280L
        private const val LONG_PRESS_TIMEOUT_MS = 520L
        private const val SEEK_ACCUMULATE_RESET_MS = 850L
        private const val FEEDBACK_DURATION_MS = 900L
        private const val BOTTOM_PASSTHROUGH_DP = 92
        private const val MIN_SWIPE_DISTANCE_DP = 10
        private const val VERTICAL_GESTURE_RATIO = 1.15f
        private const val LOCKED_ICON = "\ud83d\udd12"
        private const val UNLOCKED_ICON = "\ud83d\udd13"
        private const val ROTATE_ICON = "\u21bb"
        private const val TRACK_ICON = "轨"
        private const val PREVIOUS_ICON = "\u23ee"
        private const val NEXT_ICON = "\u23ed"
        private const val REPEAT_NONE_ICON = "\u21c4"
        private const val REPEAT_ONE_ICON = "\u267e1"
        private const val REPEAT_ALL_ICON = "\u267e"
        private const val QUEUE_ICON = "\u2630"
        private const val ZOOM_FIT_ICON = "\u9002"
        private const val ZOOM_STRETCH_ICON = "\u62c9"
        private const val ZOOM_CROP_ICON = "\u88c1"
    }
}
