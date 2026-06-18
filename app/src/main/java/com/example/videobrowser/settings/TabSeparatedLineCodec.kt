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

    fun splitFields(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var escaping = false
        line.forEach { char ->
            if (escaping) {
                current.append(
                    when (char) {
                        't' -> '\t'
                        'n' -> '\n'
                        'r' -> '\r'
                        else -> char
                    }
                )
                escaping = false
            } else {
                when (char) {
                    '\\' -> escaping = true
                    '\t' -> {
                        fields.add(current.toString())
                        current.clear()
                    }
                    else -> current.append(char)
                }
            }
        }
        if (escaping) {
            current.append('\\')
        }
        fields.add(current.toString())
        return fields
    }

    fun joinFields(fields: List<String>): String {
        return fields.joinToString(separator = "\t", transform = ::escapeField)
    }

    private fun escapeField(value: String): String {
        return buildString {
            value.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '\t' -> append("\\t")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    else -> append(char)
                }
            }
        }
    }
}
