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

internal class CursorColumnValueReader(private val cursor: Cursor) {
    private val columnIndexes = mutableMapOf<String, Int>()

    fun stringOrNull(columnName: String): String? {
        return cursor.stringOrNull(indexOf(columnName))
    }

    fun longOrNull(columnName: String): Long? {
        return cursor.longOrNull(indexOf(columnName))
    }

    fun intOrNull(columnName: String): Int? {
        return cursor.intOrNull(indexOf(columnName))
    }

    private fun indexOf(columnName: String): Int {
        return columnIndexes.getOrPut(columnName) { cursor.getColumnIndex(columnName) }
    }
}

internal fun Cursor.columnValueReader(): CursorColumnValueReader {
    return CursorColumnValueReader(this)
}
