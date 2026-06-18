package com.example.videobrowser.utils

object FileNameSanitizer {
    fun replaceInvalidCharacters(
        value: String,
        replacement: String = "_",
        collapseRuns: Boolean = false
    ): String {
        val pattern = if (collapseRuns) {
            INVALID_FILE_NAME_CHARACTER_RUN
        } else {
            INVALID_FILE_NAME_CHARACTER
        }
        return value.replace(pattern, replacement)
    }

    private val INVALID_FILE_NAME_CHARACTER = Regex("""[\u0000-\u001F\\/:*?"<>|]""")
    private val INVALID_FILE_NAME_CHARACTER_RUN = Regex("""[\u0000-\u001F\\/:*?"<>|]+""")
}
