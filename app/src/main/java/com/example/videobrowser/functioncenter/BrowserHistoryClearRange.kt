package com.example.videobrowser.functioncenter

enum class BrowserHistoryClearRange(private val durationMillis: Long?) {
    LAST_HOUR(MILLIS_PER_HOUR),
    LAST_24_HOURS(MILLIS_PER_DAY),
    LAST_7_DAYS(MILLIS_PER_DAY * 7),
    ALL(null);

    fun cutoffMillis(nowMillis: Long): Long? {
        return durationMillis?.let { duration -> nowMillis - duration }
    }
}

private const val MILLIS_PER_HOUR = 60L * 60L * 1000L
private const val MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR
