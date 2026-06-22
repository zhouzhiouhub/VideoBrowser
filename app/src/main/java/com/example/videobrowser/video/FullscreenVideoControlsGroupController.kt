package com.example.videobrowser.video

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.example.videobrowser.R
import com.example.videobrowser.utils.BrowserDrawableFactory

internal class FullscreenVideoControlsGroupController(
    private val context: Context,
    private val controlsGroup: LinearLayout,
    private val previousButton: TextView,
    private val nextButton: TextView,
    private val repeatButton: TextView,
    private val queueButton: TextView,
    val speedButton: TextView,
    private val trackButton: TextView,
    private val zoomButton: TextView,
    private val rotateButton: TextView,
    private val dp: (Int) -> Int,
    private val isLocked: () -> Boolean,
    private val notifyUserInteraction: () -> Unit,
    private val showSpeedPopup: () -> Unit,
    private val requestPreviousMedia: () -> Unit,
    private val requestNextMedia: () -> Unit,
    private val requestRepeatMode: () -> PlaybackRepeatMode?,
    private val requestPlaybackQueue: () -> Unit,
    private val requestTrackSelection: () -> Unit,
    private val requestVideoZoomMode: () -> VideoZoomMode?,
    private val requestOrientation: () -> Boolean?,
    private val showFeedback: (String) -> Unit
) {
    private var landscape = true
    private var playbackSpeed = DEFAULT_PLAYBACK_SPEED
    private var repeatMode = PlaybackRepeatMode.NONE
    private var videoZoomMode = VideoZoomMode.FIT
    private var attached = false

    fun attachTo(parent: ViewGroup) {
        if (attached) return
        attached = true
        setupControlsGroup()
        parent.addView(
            controlsGroup,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = dp(14)
                rightMargin = dp(14)
            }
        )
    }

    fun currentPlaybackSpeed(): Float {
        return playbackSpeed
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = FullscreenVideoGestureMath.normalizeSpeed(speed, DEFAULT_PLAYBACK_SPEED)
        speedButton.text = VideoGestureFeedbackFormatter.formatSpeed(playbackSpeed)
        speedButton.contentDescription = context.getString(
            R.string.video_control_speed,
            VideoGestureFeedbackFormatter.formatSpeed(playbackSpeed)
        )
    }

    fun setLandscape(isLandscape: Boolean) {
        landscape = isLandscape
        val label = context.getString(
            if (landscape) {
                R.string.video_control_rotate_to_portrait
            } else {
                R.string.video_control_rotate_to_landscape
            }
        )
        rotateButton.text = ROTATE_ICON
        rotateButton.contentDescription = label
        ViewCompat.setTooltipText(rotateButton, label)
    }

    fun setQueueControlsVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        previousButton.visibility = visibility
        nextButton.visibility = visibility
        repeatButton.visibility = visibility
        queueButton.visibility = visibility
    }

    fun setRepeatMode(mode: PlaybackRepeatMode) {
        repeatMode = mode
        repeatButton.text = when (mode) {
            PlaybackRepeatMode.NONE -> REPEAT_NONE_ICON
            PlaybackRepeatMode.ONE -> REPEAT_ONE_ICON
            PlaybackRepeatMode.ALL -> REPEAT_ALL_ICON
        }
        val label = context.getString(
            when (mode) {
                PlaybackRepeatMode.NONE -> R.string.video_control_repeat_none
                PlaybackRepeatMode.ONE -> R.string.video_control_repeat_one
                PlaybackRepeatMode.ALL -> R.string.video_control_repeat_all
            }
        )
        repeatButton.contentDescription = label
        ViewCompat.setTooltipText(repeatButton, label)
    }

    fun setVideoZoomMode(mode: VideoZoomMode) {
        videoZoomMode = mode
        zoomButton.text = when (mode) {
            VideoZoomMode.FIT -> ZOOM_FIT_ICON
            VideoZoomMode.STRETCH -> ZOOM_STRETCH_ICON
            VideoZoomMode.CROP -> ZOOM_CROP_ICON
        }
        val label = context.getString(
            when (mode) {
                VideoZoomMode.FIT -> R.string.video_control_zoom_fit
                VideoZoomMode.STRETCH -> R.string.video_control_zoom_stretch
                VideoZoomMode.CROP -> R.string.video_control_zoom_crop
            }
        )
        zoomButton.contentDescription = label
        ViewCompat.setTooltipText(zoomButton, label)
    }

    private fun setupControlsGroup() {
        controlsGroup.orientation = LinearLayout.HORIZONTAL
        controlsGroup.gravity = Gravity.CENTER_VERTICAL
        addControlButton(previousButton)
        addControlButton(nextButton)
        addControlButton(repeatButton)
        addControlButton(queueButton)
        addControlButton(speedButton, widthDp = 62)
        addControlButton(trackButton)
        addControlButton(zoomButton)
        addControlButton(rotateButton, marginEndDp = 0)

        speedButton.setOnClickListener {
            notifyUserInteraction()
            if (!isLocked()) showSpeedPopup()
        }
        setupPreviousButton()
        setupNextButton()
        setupRepeatButton()
        setupQueueButton()
        setupTrackButton()
        setupZoomButton()
        setupRotateButton()

        setPlaybackSpeed(DEFAULT_PLAYBACK_SPEED)
        setQueueControlsVisible(false)
        setRepeatMode(PlaybackRepeatMode.NONE)
        setVideoZoomMode(VideoZoomMode.FIT)
        setLandscape(true)
    }

    private fun addControlButton(
        button: TextView,
        widthDp: Int = 44,
        marginEndDp: Int = 8
    ) {
        controlsGroup.addView(
            button,
            LinearLayout.LayoutParams(
                dp(widthDp),
                dp(40)
            ).apply {
                if (marginEndDp > 0) {
                    marginEnd = dp(marginEndDp)
                }
            }
        )
    }

    private fun setupPreviousButton() {
        previousButton.text = PREVIOUS_ICON
        previousButton.contentDescription = context.getString(R.string.video_control_previous)
        ViewCompat.setTooltipText(
            previousButton,
            context.getString(R.string.video_control_previous)
        )
        previousButton.setOnClickListener {
            notifyUserInteraction()
            if (isLocked()) return@setOnClickListener
            requestPreviousMedia()
        }
    }

    private fun setupNextButton() {
        nextButton.text = NEXT_ICON
        nextButton.contentDescription = context.getString(R.string.video_control_next)
        ViewCompat.setTooltipText(nextButton, context.getString(R.string.video_control_next))
        nextButton.setOnClickListener {
            notifyUserInteraction()
            if (isLocked()) return@setOnClickListener
            requestNextMedia()
        }
    }

    private fun setupRepeatButton() {
        repeatButton.setOnClickListener {
            notifyUserInteraction()
            if (isLocked()) return@setOnClickListener
            val mode = requestRepeatMode() ?: repeatMode
            setRepeatMode(mode)
            showFeedback(repeatButton.contentDescription?.toString().orEmpty())
        }
    }

    private fun setupQueueButton() {
        val queueLabel = context.getString(R.string.video_control_queue)
        queueButton.text = QUEUE_ICON
        queueButton.contentDescription = queueLabel
        ViewCompat.setTooltipText(queueButton, queueLabel)
        queueButton.setOnClickListener {
            notifyUserInteraction()
            if (isLocked()) return@setOnClickListener
            requestPlaybackQueue()
        }
    }

    private fun setupTrackButton() {
        val trackLabel = context.getString(R.string.video_control_tracks)
        trackButton.text = TRACK_ICON
        trackButton.contentDescription = trackLabel
        ViewCompat.setTooltipText(trackButton, trackLabel)
        trackButton.setOnClickListener {
            notifyUserInteraction()
            if (isLocked()) return@setOnClickListener
            requestTrackSelection()
        }
    }

    private fun setupZoomButton() {
        zoomButton.setOnClickListener {
            notifyUserInteraction()
            if (isLocked()) return@setOnClickListener
            val mode = requestVideoZoomMode() ?: videoZoomMode.next()
            setVideoZoomMode(mode)
            showFeedback(zoomButton.contentDescription?.toString().orEmpty())
        }
    }

    private fun setupRotateButton() {
        rotateButton.setOnClickListener {
            notifyUserInteraction()
            if (isLocked()) return@setOnClickListener
            val isLandscape = requestOrientation() ?: !landscape
            setLandscape(isLandscape)
            showFeedback(ROTATE_ICON)
        }
    }

    companion object {
        fun controlTextView(context: Context, dp: (Int) -> Int): TextView {
            return TextView(context).apply {
                gravity = Gravity.CENTER
                includeFontPadding = false
                minWidth = dp(44)
                setPadding(dp(8), 0, dp(8), 0)
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
                textSize = 18f
                background = BrowserDrawableFactory.roundedBackground(
                    Color.argb(178, 0, 0, 0),
                    dp(20)
                )
                isClickable = true
                isFocusable = true
            }
        }

        const val DEFAULT_PLAYBACK_SPEED = 1f
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
