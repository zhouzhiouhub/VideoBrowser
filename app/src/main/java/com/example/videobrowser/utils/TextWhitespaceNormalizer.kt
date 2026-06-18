package com.example.videobrowser.utils

object TextWhitespaceNormalizer {
    fun collapse(value: String): String {
        return value.trim().replace(WHITESPACE_SEQUENCE, " ")
    }

    fun hasTabOrLineBreak(value: String): Boolean {
        return value.any { char -> char == '\t' || char == '\n' || char == '\r' }
    }

    private val WHITESPACE_SEQUENCE = Regex("\\s+")
}
