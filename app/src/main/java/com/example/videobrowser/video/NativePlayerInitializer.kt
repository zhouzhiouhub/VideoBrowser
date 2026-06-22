package com.example.videobrowser.video

import android.content.Context
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView

internal class NativePlayerInitializer(
    private val context: Context,
    private val playerView: PlayerView,
    private val logTag: String,
    private val mediaUri: () -> String,
    private val mimeType: () -> String?,
    private val requestHeaders: () -> Map<String, String>,
    private val userAgent: () -> String?,
    private val playbackQueue: () -> PlaybackQueue,
    private val currentMediaItemIndex: () -> Int,
    private val playbackPosition: () -> Long,
    private val playWhenReady: () -> Boolean,
    private val applyVideoEffects: (ExoPlayer) -> Unit,
    private val applyRepeatMode: (ExoPlayer) -> Unit,
    private val applyPlaybackSpeed: (ExoPlayer) -> Unit,
    private val createEventListener: (ExoPlayer) -> Player.Listener
) {
    fun initialize(): ExoPlayer {
        Log.d(
            logTag,
            "event=native-initialize uri=${mediaUri().take(180)} mime=${mimeType()}"
        )

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(requestHeaders())
        userAgent()
            ?.takeIf { it.isNotBlank() }
            ?.let { dataSourceFactory.setUserAgent(it) }

        val mediaSourceFactory = DefaultMediaSourceFactory(
            DefaultDataSource.Factory(context, dataSourceFactory)
        )
        val mediaItems = playbackQueue().items.map(PlayableMediaItemMedia3Converter::toMediaItem)

        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .also { exoPlayer ->
                applyVideoEffects(exoPlayer)
                exoPlayer.addListener(createEventListener(exoPlayer))
                playerView.player = exoPlayer
                exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, true)
                exoPlayer.setMediaItems(
                    mediaItems,
                    currentMediaItemIndex(),
                    playbackPosition()
                )
                applyRepeatMode(exoPlayer)
                applyPlaybackSpeed(exoPlayer)
                exoPlayer.playWhenReady = playWhenReady()
                exoPlayer.prepare()
            }
    }
}
