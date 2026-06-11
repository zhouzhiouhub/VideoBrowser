package com.example.videobrowser.video

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
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
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.example.videobrowser.R
import kotlin.math.abs
import kotlin.math.roundToInt

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
    var onExitFullscreen: (() -> Unit)? = null
    var onTrackSelectionRequested: (() -> Unit)? = null

    private val audioManager =
        activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val feedbackHandler = Handler(Looper.getMainLooper())
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val swipeStartDistance by lazy { maxOf(dp(MIN_SWIPE_DISTANCE_DP), touchSlop) }
    private val speedOptions = VideoSpeedOptions.menuSpeeds()

    private val exitButton = ImageButton(context)
    private val lockButton = controlTextView()
    private val speedButton = controlTextView()
    private val trackButton = controlTextView()
    private val rotateButton = controlTextView()
    private val controlsGroup = LinearLayout(context)
    private val feedbackView = TextView(context)
    private var speedPopup: PopupWindow? = null

    private var locked = false
    private var landscape = true
    private var playbackSpeed = DEFAULT_PLAYBACK_SPEED
    private var savedWindowBrightness: Float? = null
    private var touchStartedOnControl = false
    private var touchStartedInBottomPassthrough = false
    private var activeGesture = VerticalGesture.NONE
    private var tapCandidate = false
    private var longPressActive = false
    private var downZone = ScreenZone.CENTER
    private var seekStartPositionMs: Long? = null
    private var seekDurationMs: Long? = null
    private var pendingHorizontalSeekMs = 0L
    private var pendingSeekTargetMs: Long? = null
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L
    private var initialBrightness = DEFAULT_BRIGHTNESS
    private var initialVolume = 0
    private var pendingTapZone = ScreenZone.NONE
    private var pendingTapTime = 0L
    private var seekAccumulatorDirection = 0
    private var seekAccumulatorCount = 0

    private val hideFeedbackRunnable = Runnable {
        feedbackView.visibility = View.GONE
    }

    private val clearPendingTapRunnable = Runnable {
        pendingTapZone = ScreenZone.NONE
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

    fun showOverlay() {
        if (visibility != View.VISIBLE) {
            savedWindowBrightness = activity.window.attributes.screenBrightness
        }
        visibility = View.VISIBLE
        bringToFront()
        setLocked(false, announce = false)
    }

    fun hideOverlay() {
        speedPopup?.dismiss()
        speedPopup = null
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

    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = normalizeSpeed(speed)
        speedButton.text = VideoGestureFeedbackFormatter.formatSpeed(playbackSpeed)
        speedButton.contentDescription = context.getString(
            R.string.video_control_speed,
            VideoGestureFeedbackFormatter.formatSpeed(playbackSpeed)
        )
    }

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

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (visibility != View.VISIBLE || width <= 0 || height <= 0) {
            return false
        }

        if (event.isWakeControlsAction()) {
            notifyUserInteraction()
        }

        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
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

        if (locked) {
            if (event.isFinishedAction()) {
                resetTouchState()
            }
            return true
        }

        return handleGestureEvent(event)
    }

    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        if (visibility == View.VISIBLE && event.isWakeControlsAction()) {
            notifyUserInteraction()
        }
        return super.dispatchHoverEvent(event)
    }

    override fun onDetachedFromWindow() {
        hideOverlay()
        super.onDetachedFromWindow()
    }

    private fun setupExitButton() {
        val label = context.getString(R.string.video_control_exit_fullscreen)
        exitButton.apply {
            background = roundedBackground(Color.argb(178, 0, 0, 0), dp(20))
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

    private fun setupControlsGroup() {
        controlsGroup.orientation = LinearLayout.HORIZONTAL
        controlsGroup.gravity = Gravity.CENTER_VERTICAL
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
        val trackLabel = context.getString(R.string.video_control_tracks)
        trackButton.text = TRACK_ICON
        trackButton.contentDescription = trackLabel
        ViewCompat.setTooltipText(trackButton, trackLabel)
        trackButton.setOnClickListener {
            notifyUserInteraction()
            if (locked) return@setOnClickListener
            onTrackSelectionRequested?.invoke()
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
            background = roundedBackground(Color.argb(196, 0, 0, 0), dp(20))
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
            background = roundedBackground(Color.argb(178, 0, 0, 0), dp(20))
        }
    }

    private fun handleGestureEvent(event: MotionEvent): Boolean {
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
                    activeGesture = if (downZone == ScreenZone.LEFT) {
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

    private fun beginHorizontalSeek(deltaX: Float) {
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

    private fun finishHorizontalSeek(commit: Boolean) {
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

    private fun updateBrightness(deltaY: Float) {
        val deltaRatio = -deltaY / height.toFloat()
        val brightness = (initialBrightness + deltaRatio).coerceIn(MIN_BRIGHTNESS, 1f)
        val attributes = activity.window.attributes
        attributes.screenBrightness = brightness
        activity.window.attributes = attributes
        showFeedback(
            "$BRIGHTNESS_ICON ${(brightness * 100).roundToInt()}%"
        )
    }

    private fun updateVolume(deltaY: Float) {
        val minVolume = streamMinVolume()
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (maxVolume <= minVolume) return

        val deltaRatio = -deltaY / height.toFloat()
        val nextVolume = (initialVolume + deltaRatio * (maxVolume - minVolume))
            .roundToInt()
            .coerceIn(minVolume, maxVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nextVolume, 0)
        showFeedback(
            "$VOLUME_ICON ${volumePercent(nextVolume, minVolume, maxVolume)}%"
        )
    }

    private fun handleTap(upX: Float, eventTime: Long) {
        notifyUserInteraction()
        when (val zone = screenZoneFor(upX)) {
            ScreenZone.CENTER -> {
                clearPendingSideTap()
                onTogglePlayPause?.invoke()
            }
            ScreenZone.LEFT,
            ScreenZone.RIGHT -> registerSideTap(zone, eventTime)
            ScreenZone.NONE -> Unit
        }
    }

    private fun registerSideTap(zone: ScreenZone, eventTime: Long) {
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

    private fun handleDoubleTap(zone: ScreenZone) {
        val direction = if (zone == ScreenZone.LEFT) -1 else 1
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

    private fun triggerLongPress() {
        if (locked || longPressActive || activeGesture != VerticalGesture.NONE || !downZone.isSide()) {
            return
        }
        longPressActive = true
        tapCandidate = false
        clearPendingSideTap()
        clearSeekAccumulator()
        val direction = if (downZone == ScreenZone.LEFT) -1 else 1
        onDirectionalLongPressStart?.invoke(direction)
        showFeedback(
            VideoGestureFeedbackFormatter.formatSpeed(VideoSpeedOptions.longPressSpeed),
            autoHide = false
        )
    }

    private fun stopLongPress() {
        if (!longPressActive) return
        longPressActive = false
        onDirectionalLongPressEnd?.invoke()
        feedbackHandler.removeCallbacks(hideFeedbackRunnable)
        feedbackView.visibility = View.GONE
    }

    private fun showSpeedPopup() {
        speedPopup?.dismiss()
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(6), 0, dp(6))
            background = roundedBackground(Color.argb(235, 18, 18, 18), dp(8))
        }

        speedOptions.forEach { speed ->
            content.addView(
                TextView(context).apply {
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                    minHeight = dp(38)
                    text = VideoGestureFeedbackFormatter.formatSpeed(speed)
                    setTextColor(if (abs(speed - playbackSpeed) < 0.01f) Color.WHITE else Color.LTGRAY)
                    setTypeface(typeface, if (abs(speed - playbackSpeed) < 0.01f) Typeface.BOLD else Typeface.NORMAL)
                    textSize = 14f
                    setOnClickListener {
                        notifyUserInteraction()
                        setPlaybackSpeed(speed)
                        onPlaybackSpeedSelected?.invoke(speed)
                        speedPopup?.dismiss()
                        showFeedback(VideoGestureFeedbackFormatter.formatSpeed(speed))
                    }
                },
                LinearLayout.LayoutParams(dp(96), dp(38))
            )
        }

        speedPopup = PopupWindow(
            content,
            dp(96),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isOutsideTouchable = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = dp(8).toFloat()
            }
        }
        speedPopup?.showAsDropDown(speedButton, 0, dp(6))
    }

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

    private fun updateLockUi() {
        val controlLabel = context.getString(
            if (locked) R.string.video_control_unlock else R.string.video_control_lock
        )
        lockButton.text = if (locked) UNLOCKED_ICON else LOCKED_ICON
        lockButton.contentDescription = controlLabel
        ViewCompat.setTooltipText(lockButton, controlLabel)
        controlsGroup.visibility = if (locked) View.GONE else View.VISIBLE
        if (locked) {
            speedPopup?.dismiss()
            speedPopup = null
        }
    }

    private fun isControlPoint(x: Float, y: Float): Boolean {
        return isPointInside(exitButton, x, y) ||
            isPointInside(lockButton, x, y) ||
            (!locked && isPointInside(controlsGroup, x, y))
    }

    private fun isPointInside(view: View, x: Float, y: Float): Boolean {
        if (view.visibility != View.VISIBLE) return false
        return x >= view.left &&
            x <= view.right &&
            y >= view.top &&
            y <= view.bottom
    }

    private fun currentWindowBrightness(): Float {
        val current = activity.window.attributes.screenBrightness
        if (current >= 0f) {
            return current.coerceIn(MIN_BRIGHTNESS, 1f)
        }
        return runCatching {
            Settings.System.getInt(
                activity.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            ) / 255f
        }.getOrDefault(DEFAULT_BRIGHTNESS).coerceIn(MIN_BRIGHTNESS, 1f)
    }

    private fun restoreWindowBrightness() {
        val saved = savedWindowBrightness ?: return
        val attributes = activity.window.attributes
        attributes.screenBrightness = saved
        activity.window.attributes = attributes
        savedWindowBrightness = null
    }

    private fun streamMinVolume(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        } else {
            0
        }
    }

    private fun volumePercent(volume: Int, minVolume: Int, maxVolume: Int): Int {
        return (((volume - minVolume).toFloat() / (maxVolume - minVolume)) * 100)
            .roundToInt()
            .coerceIn(0, 100)
    }

    private fun showFeedback(text: String, autoHide: Boolean = true) {
        feedbackView.text = text
        feedbackView.visibility = View.VISIBLE
        feedbackView.bringToFront()
        feedbackHandler.removeCallbacks(hideFeedbackRunnable)
        if (autoHide) {
            feedbackHandler.postDelayed(hideFeedbackRunnable, FEEDBACK_DURATION_MS)
        }
    }

    private fun notifyUserInteraction() {
        onUserInteraction?.invoke()
    }

    private fun resetTouchState() {
        activeGesture = VerticalGesture.NONE
        tapCandidate = false
        touchStartedOnControl = false
        touchStartedInBottomPassthrough = false
        downZone = ScreenZone.CENTER
    }

    private fun clearPendingSideTap() {
        pendingTapZone = ScreenZone.NONE
        pendingTapTime = 0L
        feedbackHandler.removeCallbacks(clearPendingTapRunnable)
    }

    private fun clearSeekAccumulator() {
        seekAccumulatorDirection = 0
        seekAccumulatorCount = 0
        feedbackHandler.removeCallbacks(clearSeekAccumulatorRunnable)
    }

    private fun roundedBackground(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    private fun normalizeSpeed(speed: Float): Float {
        return if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) {
            speed
        } else {
            DEFAULT_PLAYBACK_SPEED
        }
    }

    private fun screenZoneFor(x: Float): ScreenZone {
        return when {
            x < 0f || width <= 0 -> ScreenZone.NONE
            x < width * LEFT_ZONE_RATIO -> ScreenZone.LEFT
            x >= width * (1f - RIGHT_ZONE_RATIO) -> ScreenZone.RIGHT
            else -> ScreenZone.CENTER
        }
    }

    private fun bottomPassthroughHeight(): Int {
        return dp(BOTTOM_PASSTHROUGH_DP)
    }

    private fun MotionEvent.isFinishedAction(): Boolean {
        return actionMasked == MotionEvent.ACTION_UP ||
            actionMasked == MotionEvent.ACTION_CANCEL
    }

    private fun MotionEvent.isWakeControlsAction(): Boolean {
        return actionMasked == MotionEvent.ACTION_DOWN ||
            actionMasked == MotionEvent.ACTION_MOVE ||
            actionMasked == MotionEvent.ACTION_UP ||
            actionMasked == MotionEvent.ACTION_HOVER_ENTER ||
            actionMasked == MotionEvent.ACTION_HOVER_MOVE
    }

    private fun ScreenZone.isSide(): Boolean {
        return this == ScreenZone.LEFT || this == ScreenZone.RIGHT
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).roundToInt()
    }

    private enum class ScreenZone {
        LEFT,
        CENTER,
        RIGHT,
        NONE
    }

    private enum class VerticalGesture {
        NONE,
        HORIZONTAL_SEEK,
        BRIGHTNESS,
        VOLUME
    }

    private companion object {
        private const val DEFAULT_PLAYBACK_SPEED = 1f
        private const val LONG_PRESS_PLAYBACK_SPEED = 2f
        private const val DEFAULT_BRIGHTNESS = 0.5f
        private const val MIN_BRIGHTNESS = 0.02f
        private const val SEEK_STEP_MS = 10_000L
        private const val SEEK_STEP_SECONDS = 10
        private const val TAP_MAX_DURATION_MS = 260L
        private const val DOUBLE_TAP_TIMEOUT_MS = 280L
        private const val LONG_PRESS_TIMEOUT_MS = 520L
        private const val SEEK_ACCUMULATE_RESET_MS = 850L
        private const val FEEDBACK_DURATION_MS = 900L
        private const val BOTTOM_PASSTHROUGH_DP = 92
        private const val MIN_SWIPE_DISTANCE_DP = 10
        private const val LEFT_ZONE_RATIO = 0.3f
        private const val RIGHT_ZONE_RATIO = 0.3f
        private const val VERTICAL_GESTURE_RATIO = 1.15f
        private const val LOCKED_ICON = "\ud83d\udd12"
        private const val UNLOCKED_ICON = "\ud83d\udd13"
        private const val ROTATE_ICON = "\u21bb"
        private const val TRACK_ICON = "轨"
        private const val BRIGHTNESS_ICON = "\u2600"
        private const val VOLUME_ICON = "\ud83d\udd0a"
    }
}
