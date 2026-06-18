package com.example.videobrowser.utils

import java.text.DateFormat
import java.util.Date

internal object ShortDateTimeFormatter {
    fun format(timestampMillis: Long): String {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            .format(Date(timestampMillis))
    }
}
