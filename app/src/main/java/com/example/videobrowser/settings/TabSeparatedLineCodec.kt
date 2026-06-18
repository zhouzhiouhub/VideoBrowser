package com.example.videobrowser.settings

internal object TabSeparatedLineCodec {
    fun splitPair(line: String): Pair<String, String>? {
        val separatorIndex = line.indexOf('\t')
        if (separatorIndex <= 0 || separatorIndex >= line.lastIndex) {
            return null
        }
        return line.substring(0, separatorIndex) to line.substring(separatorIndex + 1)
    }

    fun joinPair(first: String, second: String): String {
        return "$first\t$second"
    }
}
