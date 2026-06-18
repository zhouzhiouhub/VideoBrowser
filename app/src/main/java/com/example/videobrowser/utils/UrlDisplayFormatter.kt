package com.example.videobrowser.utils

import java.nio.charset.StandardCharsets

internal object UrlDisplayFormatter {
    fun displayUrl(url: String): String {
        val parsedUri = SafeUriParser.parse(url) ?: return decodePercentEncoded(url)
        val scheme = parsedUri.scheme?.let { "$it://" }.orEmpty()
        val authority = parsedUri.rawAuthority ?: return decodePercentEncoded(url)
        val path = decodePercentEncoded(parsedUri.rawPath.orEmpty())
        val query = parsedUri.rawQuery
            ?.let { "?${decodePercentEncoded(it)}" }
            .orEmpty()
        val fragment = parsedUri.rawFragment
            ?.let { "#${decodePercentEncoded(it)}" }
            .orEmpty()
        return "$scheme$authority$path$query$fragment"
    }

    private fun decodePercentEncoded(value: String): String {
        if (!value.contains('%')) {
            return value
        }

        val decoded = StringBuilder(value.length)
        var index = 0
        while (index < value.length) {
            if (value[index] != '%' || !hasHexByteAt(value, index)) {
                decoded.append(value[index])
                index += 1
                continue
            }

            val bytes = mutableListOf<Byte>()
            while (index < value.length && value[index] == '%' && hasHexByteAt(value, index)) {
                bytes += ((hexValue(value[index + 1]) shl 4) or hexValue(value[index + 2])).toByte()
                index += 3
            }
            decoded.append(bytes.toByteArray().toString(StandardCharsets.UTF_8))
        }
        return decoded.toString()
    }

    private fun hasHexByteAt(value: String, index: Int): Boolean {
        return index + 2 < value.length &&
            hexValue(value[index + 1]) >= 0 &&
            hexValue(value[index + 2]) >= 0
    }

    private fun hexValue(char: Char): Int {
        return when (char) {
            in '0'..'9' -> char - '0'
            in 'a'..'f' -> char - 'a' + 10
            in 'A'..'F' -> char - 'A' + 10
            else -> -1
        }
    }
}
