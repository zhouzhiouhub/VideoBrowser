package com.example.videobrowser.settings

import com.example.videobrowser.utils.TextWhitespaceNormalizer

internal object SettingsCssSelectorNormalizer {
    fun normalize(selector: String): String? {
        val collapsed = TextWhitespaceNormalizer.collapse(selector)
        val normalized = stabilizeSelector(collapsed)
        if (normalized.isEmpty() || normalized.length > MAX_USER_ELEMENT_SELECTOR_LENGTH) {
            return null
        }
        if (TextWhitespaceNormalizer.hasTabOrLineBreak(normalized)) {
            return null
        }
        if (UNSAFE_SELECTOR_PATTERN.containsMatchIn(normalized)) {
            return null
        }
        if (UNSUPPORTED_SELECTOR_PATTERN.containsMatchIn(normalized)) {
            return null
        }
        return normalized
    }

    private fun stabilizeSelector(selector: String): String {
        if (!selector.contains(":nth-of-type", ignoreCase = true) ||
            !STABLE_TOKEN_PATTERN.containsMatchIn(selector)
        ) {
            return selector
        }
        return POSITIONAL_SEGMENT_PATTERN
            .replace(selector, "")
            .let(TextWhitespaceNormalizer::collapse)
    }

    private val UNSAFE_SELECTOR_PATTERN = Regex("[{};<>]")
    private val UNSUPPORTED_SELECTOR_PATTERN =
        Regex(":has\\(|:contains\\(|:matches\\(|:xpath\\(|javascript:|expression\\(", RegexOption.IGNORE_CASE)
    private val POSITIONAL_SEGMENT_PATTERN =
        Regex(":nth-of-type\\(\\s*\\d+\\s*\\)", RegexOption.IGNORE_CASE)
    private val STABLE_TOKEN_PATTERN = Regex("[#.][A-Za-z_][A-Za-z0-9_-]*")
}

