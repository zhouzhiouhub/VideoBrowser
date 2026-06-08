package com.example.videobrowser.version

object AppVersionFormatter {
    fun formatCommitCount(commitCount: Int): String {
        val count = commitCount.coerceAtLeast(0)
        val major = count / 1000
        val hundreds = (count / 100) % 10
        val tens = (count / 10) % 10
        val ones = count % 10
        return "$major.$hundreds.$tens.$ones"
    }
}
