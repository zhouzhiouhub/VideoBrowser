package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“网页播放历史记录模块”。
 * 网页里的 JavaScript 会通过 VideoBrowserNativeBridge 上报视频进度，本类负责把这些进度保存成播放历史。
 * 主要职责：跳过无痕模式、过滤不可分享 URL、按页面 URL 节流保存，并写入 PlaybackHistoryRepository。
 * 阅读顺序：先看 record，再看 shouldThrottle。
 */
import android.os.SystemClock

/**
 * 网页视频播放历史记录器。
 *
 * MainActivity 只需要在网页上报播放进度时调用 record；本类负责判断是否应该保存以及如何组装 PlaybackProgress。
 *
 * @param playbackHistoryRepository 参数类型为 `PlaybackHistoryRepository`，表示播放历史持久化仓库，用来保存网页播放进度。
 * @param isPrivateBrowsingEnabled 参数类型为 `() -> Boolean`，表示读取当前是否处于无痕模式的回调；无痕模式不会写历史。
 * @param currentShareableUrl 参数类型为 `() -> String?`，表示读取当前可分享页面 URL 的回调，用作网页媒体身份。
 * @param isShareableUrl 参数类型为 `(String) -> Boolean`，表示判断 URL 是否适合记录到历史的回调。
 * @param defaultVideoSpeed 参数类型为 `() -> Float`，表示读取默认播放倍速的回调，用来补齐播放历史字段。
 * @param currentPageTitle 参数类型为 `() -> String`，表示读取当前页面标题的回调，用来展示播放历史条目。
 * @param elapsedRealtime 参数类型为 `() -> Long`，表示读取单调递增时间的回调，用于节流判断。
 * @param currentTimeMillis 参数类型为 `() -> Long`，表示读取墙钟时间的回调，用作播放历史更新时间。
 */
class WebPlaybackHistoryRecorder(
    private val playbackHistoryRepository: PlaybackHistoryRepository,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val currentShareableUrl: () -> String?,
    private val isShareableUrl: (String) -> Boolean,
    private val defaultVideoSpeed: () -> Float,
    private val currentPageTitle: () -> String,
    private val elapsedRealtime: () -> Long = SystemClock::elapsedRealtime,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) {
    private var lastSavedIdentity: String? = null
    private var lastSavedElapsedRealtime = 0L

    /**
     * 函数 `record`：按当前网页 URL 保存一次网页视频播放进度。
     *
     * 初学者阅读提示：网页会频繁上报播放时间，所以这里会先做无痕、URL 和节流检查，再真正写入仓库。
     *
     * @param positionMs 参数类型为 `Double`，表示网页视频当前播放位置，单位是毫秒。
     * @param durationMs 参数类型为 `Double`，表示网页视频总时长，单位是毫秒。
     */
    fun record(positionMs: Double, durationMs: Double) {
        if (isPrivateBrowsingEnabled()) {
            return
        }
        val pageUrl = currentShareableUrl()?.takeIf { it.isNotBlank() } ?: return
        if (!isShareableUrl(pageUrl)) {
            return
        }
        val nowElapsed = elapsedRealtime()
        if (shouldThrottle(pageUrl, nowElapsed)) {
            return
        }
        lastSavedIdentity = pageUrl
        lastSavedElapsedRealtime = nowElapsed

        playbackHistoryRepository.save(
            PlaybackProgress(
                mediaIdentity = pageUrl,
                positionMs = positionMs.toLong().coerceAtLeast(0L),
                durationMs = durationMs.toLong().coerceAtLeast(0L),
                speed = defaultVideoSpeed(),
                updatedAtMillis = currentTimeMillis(),
                title = currentPageTitle(),
                source = PlaybackHistorySource.WEB_PAGE
            ),
            privateBrowsing = false
        )
    }

    /**
     * 函数 `shouldThrottle`：判断同一页面 URL 是否刚刚保存过。
     *
     * @param pageUrl 参数类型为 `String`，表示当前网页 URL，用来和上一次保存的媒体身份比较。
     * @param nowElapsed 参数类型为 `Long`，表示当前单调递增时间，单位是毫秒。
     * @return 返回 `Boolean`，true 表示这次上报离上次保存太近，应跳过。
     */
    private fun shouldThrottle(pageUrl: String, nowElapsed: Long): Boolean {
        return lastSavedIdentity == pageUrl &&
            nowElapsed - lastSavedElapsedRealtime < SAVE_THROTTLE_MS
    }

    private companion object {
        private const val SAVE_THROTTLE_MS = 5_000L
    }
}
