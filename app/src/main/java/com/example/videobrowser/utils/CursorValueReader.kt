package com.example.videobrowser.utils

import android.database.Cursor

internal fun Cursor.stringOrNull(index: Int): String? {
    return if (index >= 0 && !isNull(index)) getString(index) else null
}

internal fun Cursor.longOrNull(index: Int): Long? {
    return if (index >= 0 && !isNull(index)) getLong(index) else null
}

internal fun Cursor.intOrNull(index: Int): Int? {
    return if (index >= 0 && !isNull(index)) getInt(index) else null
}
