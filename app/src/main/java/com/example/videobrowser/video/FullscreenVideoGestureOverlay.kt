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
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.example.videobrowser.R
import kotlin.math.abs
import kotlin.math.roundToInt

class FullscreenVideoGestureOverlay(
    private val activity: Activity
) : FrameLayout(activity) {
    var onSeekBy: ((Long) -> Unit)? = null
    var onPlaybackSpeedSelected: ((Float) -> Unit)? = null
    var onToggleOrientation: (() -> Boolean)? = null

    private val audioManager =
        activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val feedbackHandler = Handler(Looper.getMainLooper())
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val speedOptions = floatArrayOf(0.75f, 1f, 1.25f, 1.5f, 2f, 3f)

    private val lockButton = controlTextView()
    private val speedButton = controlTextView()
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
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L
    private var initialBrightness = DEFAULT_BRIGHTNESS
    private var initialVolume = 0

    private val hideFeedbackRunnable = Runnable {
        feedbackView.visibility = View.GONE
    }

    init {
        visibility = View.GONE
        isClickable = true
        isFocusable = false
        setBackgroundColor(Color.TRANSPARENT)

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
        restoreWindowBrightness()
        setLocked(false, announce = false)
        activeGesture = VerticalGesture.NONE
        touchStartedOnControl = false
        touchStartedInBottomPassthrough = false
        feedbackHandler.removeCallbacks(hideFeedbackRunnable)
        feedbackView.visibility = View.GONE
        visibility = View.GONE
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = normalizeSpeed(speed)
        speedButton.text = formatSpeed(playbackSpeed)
        speedButton.contentDescription = context.getString(
            R.string.video_control_speed,
            formatSpeed(playbackSpeed)
        )
    }

    fun setLandscape(isLandscape: Boolean) {
        landscape = isLandscape
        val label = if (landscape) {
            context.getString(R.string.video_control_rotate_to_portrait)
        } else {
            context.getString(R.string.video_control_rotate_to_landscape)
        }
        rotateButton.text = label
        rotateButton.contentDescription = label
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (visibility != View.VISIBLE || width <= 0 || height <= 0) {
            return false
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

    private fun setupLockButton() {
        lockButton.setOnClickListener {
            setLocked(!locked, announce = true)
        }
        addView(
            lockButton,
            LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(38)
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
                dp(36)
            ).apply {
                marginEnd = dp(8)
            }
        )
        controlsGroup.addView(
            rotateButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(36)
            )
        )

        speedButton.setOnClickListener {
            if (!locked) showSpeedPopup()
        }
        rotateButton.setOnClickListener {
            if (locked) return@setOnClickListener
            val isLandscape = onToggleOrientation?.invoke() ?: !landscape
            setLandscape(isLandscape)
            showFeedback(
                if (isLandscape) {
                    context.getString(R.string.video_feedback_landscape)
                } else {
                    context.getString(R.string.video_feedback_portrait)
                }
            )
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
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            textSize = 16f
            setPadding(dp(18), 0, dp(18), 0)
            background = roundedBackground(Color.argb(190, 0, 0, 0), dp(18))
        }
        addView(
            feedbackView,
            LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(42)
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
            minWidth = dp(52)
            setPadding(dp(12), 0, dp(12), 0)
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            textSize = 13f
            background = roundedBackground(Color.argb(178, 0, 0, 0), dp(18))
        }
    }

    private fun handleGestureEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downTime = event.eventTime
                initialBrightness = currentWindowBrightness()
                initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                activeGesture = VerticalGesture.NONE
                tapCandidate = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - downX
                val deltaY = event.y - downY
                if (abs(deltaX) > touchSlop || abs(deltaY) > touchSlop) {
                    tapCandidate = false
                }
                if (activeGesture == VerticalGesture.NONE &&
                    abs(deltaY) > touchSlop &&
                    abs(deltaY) > abs(deltaX)
                ) {
                    activeGesture = if (downX < width / 2f) {
                        VerticalGesture.BRIGHTNESS
                    } else {
                        VerticalGesture.VOLUME
                    }
                }
                when (activeGesture) {
                    VerticalGesture.BRIGHTNESS -> updateBrightness(deltaY)
                    VerticalGesture.VOLUME -> updateVolume(deltaY)
                    VerticalGesture.NONE -> Unit
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (tapCandidate && event.eventTime - downTime <= TAP_MAX_DURATION_MS) {
                    handleTap(event.x)
                }
                resetTouchState()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                resetTouchState()
                return true
            }
        }
        return true
    }

    private fun updateBrightness(deltaY: Float) {
        val deltaRatio = (downY - (downY + deltaY)) / height.toFloat()
        val brightness = (initialBrightness + deltaRatio).coerceIn(MIN_BRIGHTNESS, 1f)
        val attributes = activity.window.attributes
        attributes.screenBrightness = brightness
        activity.window.attributes = attributes
        showFeedback(
            context.getString(
                R.string.video_feedback_brightness,
                (brightness * 100).roundToInt()
            )
        )
    }

    private fun updateVolume(deltaY: Float) {
        val minVolume = streamMinVolume()
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (maxVolume <= minVolume) return

        val deltaRatio = (downY - (downY + deltaY)) / height.toFloat()
        val nextVolume = (initialVolume + deltaRatio * (maxVolume - minVolume))
            .roundToInt()
            .coerceIn(minVolume, maxVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nextVolume, 0)
        showFeedback(
            context.getString(
                R.string.video_feedback_volume,
                volumePercent(nextVolume, minVolume, maxVolume)
            )
        )
    }

    private fun handleTap(upX: Float) {
        val offsetMs = if (upX < width / 2f) -SEEK_STEP_MS else SEEK_STEP_MS
        onSeekBy?.invoke(offsetMs)
        showFeedback(
            if (offsetMs < 0) {
                context.getString(R.string.video_feedback_rewind)
            } else {
                context.getString(R.string.video_feedback_forward)
            }
        )
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
                    text = formatSpeed(speed)
                    setTextColor(if (abs(speed - playbackSpeed) < 0.01f) Color.WHITE else Color.LTGRAY)
                    setTypeface(typeface, if (abs(speed - playbackSpeed) < 0.01f) Typeface.BOLD else Typeface.NORMAL)
                    textSize = 14f
                    setOnClickListener {
                        setPlaybackSpeed(speed)
                        onPlaybackSpeedSelected?.invoke(speed)
                        speedPopup?.dismiss()
                        showFeedback(context.getString(R.string.video_feedback_speed, formatSpeed(speed)))
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
        locked = value
        updateLockUi()
        if (announce) {
            showFeedback(
                context.getString(
                    if (locked) R.string.video_feedback_locked else R.string.video_feedback_unlocked
                )
            )
        }
    }

    private fun updateLockUi() {
        lockButton.text = context.getString(
            if (locked) R.string.video_control_unlock else R.string.video_control_lock
        )
        lockButton.contentDescription = lockButton.text
        controlsGroup.visibility = if (locked) View.GONE else View.VISIBLE
        if (locked) {
            speedPopup?.dismiss()
            speedPopup = null
        }
    }

    private fun isControlPoint(x: Float, y: Float): Boolean {
        return isPointInside(lockButton, x, y) ||
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

    private fun showFeedback(text: String) {
        feedbackView.text = text
        feedbackView.visibility = View.VISIBLE
        feedbackView.bringToFront()
        feedbackHandler.removeCallbacks(hideFeedbackRunnable)
        feedbackHandler.postDelayed(hideFeedbackRunnable, FEEDBACK_DURATION_MS)
    }

    private fun resetTouchState() {
        activeGesture = VerticalGesture.NONE
        tapCandidate = false
        touchStartedOnControl = false
        touchStartedInBottomPassthrough = false
    }

    private fun roundedBackground(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    private fun formatSpeed(speed: Float): String {
        return if (speed == speed.toInt().toFloat()) {
            "${speed.toInt()}x"
        } else {
            "${speed}x"
        }
    }

    private fun normalizeSpeed(speed: Float): Float {
        return if (!speed.isNaN() && !speed.isInfinite() && speed > 0f) {
            speed
        } else {
            DEFAULT_PLAYBACK_SPEED
        }
    }

    private fun bottomPassthroughHeight(): Int {
        return dp(BOTTOM_PASSTHROUGH_DP)
    }

    private fun MotionEvent.isFinishedAction(): Boolean {
        return actionMasked == MotionEvent.ACTION_UP ||
            actionMasked == MotionEvent.ACTION_CANCEL
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).roundToInt()
    }

    private enum class VerticalGesture {
        NONE,
        BRIGHTNESS,
        VOLUME
    }

    private companion object {
        private const val DEFAULT_PLAYBACK_SPEED = 1f
        private const val DEFAULT_BRIGHTNESS = 0.5f
        private const val MIN_BRIGHTNESS = 0.02f
        private const val SEEK_STEP_MS = 10_000L
        private const val TAP_MAX_DURATION_MS = 260L
        private const val FEEDBACK_DURATION_MS = 900L
        private const val BOTTOM_PASSTHROUGH_DP = 92
    }
}
