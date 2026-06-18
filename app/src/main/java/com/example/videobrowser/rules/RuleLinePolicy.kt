package com.example.videobrowser.rules

import java.util.Locale

object RuleLinePolicy {
    fun shouldIgnore(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.isEmpty() ||
            trimmed.startsWith("!") ||
            trimmed.startsWith("# ") ||
            trimmed == "#"
    }

    fun isSafeSelector(selector: String): Boolean {
        val value = selector.trim()
        if (value.isEmpty() || value.length > MAX_SELECTOR_LENGTH) {
            return false
        }
        if (value.any { char -> char == '{' || char == '}' || char == ';' || char == '<' || char == '>' }) {
            return false
        }
        val lowered = value.lowercase(Locale.US)
        return !UNSUPPORTED_SELECTOR_TOKENS.any { token -> lowered.contains(token) }
    }

    fun isScriptletRuleLine(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.contains("##+js(") ||
            trimmed.contains("#%#")
    }

    private const val MAX_SELECTOR_LENGTH = 200

    private val UNSUPPORTED_SELECTOR_TOKENS = listOf(
        ":has(",
        ":contains(",
        ":matches(",
        ":xpath(",
        "javascript:",
        "expression("
    )
}
