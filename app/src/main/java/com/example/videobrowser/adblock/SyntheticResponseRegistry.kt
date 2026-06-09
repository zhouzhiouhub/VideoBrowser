package com.example.videobrowser.adblock

import java.util.Locale

class SyntheticResponseRegistry(
    private val specs: Map<String, SyntheticResponseSpec> = defaultSpecs()
) {
    fun get(name: String?): SyntheticResponseSpec? {
        val normalizedName = normalizeName(name) ?: return null
        return specs[normalizedName]
    }

    fun contains(name: String?): Boolean {
        return get(name) != null
    }

    private fun normalizeName(name: String?): String? {
        val value = name
            ?.trim()
            ?.lowercase(Locale.US)
            ?: return null
        if (value.isEmpty() || value.contains("://") || value.any { char -> char == '/' || char == '\\' }) {
            return null
        }
        return value
    }

    companion object {
        const val NOOP_JS = "noopjs"
        const val NOOP_CSS = "noopcss"
        const val NOOP_TEXT = "nooptext"

        private fun defaultSpecs(): Map<String, SyntheticResponseSpec> {
            return listOf(
                SyntheticResponseSpec(
                    name = NOOP_JS,
                    mimeType = "application/javascript",
                    body = "/* noop */\n".toByteArray(Charsets.UTF_8)
                ),
                SyntheticResponseSpec(
                    name = NOOP_CSS,
                    mimeType = "text/css",
                    body = "/* noop */\n".toByteArray(Charsets.UTF_8)
                ),
                SyntheticResponseSpec(
                    name = NOOP_TEXT,
                    mimeType = "text/plain",
                    body = ByteArray(0)
                )
            ).associateBy { spec -> spec.name }
        }
    }
}
