package com.example.videobrowser.video

import androidx.media3.common.MimeTypes

object LocalSubtitleMatcher {
    data class Document(
        val uri: String,
        val name: String,
        val mimeType: String?
    )

    private val subtitleMimeTypesByExtension = mapOf(
        "srt" to MimeTypes.APPLICATION_SUBRIP,
        "vtt" to MimeTypes.TEXT_VTT,
        "ass" to MimeTypes.TEXT_SSA,
        "ssa" to MimeTypes.TEXT_SSA,
        "ttml" to MimeTypes.APPLICATION_TTML,
        "dfxp" to MimeTypes.APPLICATION_TTML
    )
    private val supportedSubtitleMimeTypes = subtitleMimeTypesByExtension.values.toSet()
    private val languagePattern = Regex("^[A-Za-z]{2,3}(?:-[A-Za-z0-9]+)*$")

    fun findSubtitleCandidates(
        mediaName: String?,
        documents: List<Document>
    ): List<ExternalSubtitleCandidate> {
        val mediaStem = mediaName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { it.substringBeforeLast('.', missingDelimiterValue = it) }
            ?: return emptyList()

        return documents
            .mapNotNull { document -> document.toSubtitleCandidate(mediaStem) }
            .sortedBy { it.label?.lowercase().orEmpty() }
    }

    private fun Document.toSubtitleCandidate(mediaStem: String): ExternalSubtitleCandidate? {
        val normalizedName = name.trim().takeIf { it.isNotEmpty() } ?: return null
        val subtitleStem = normalizedName.substringBeforeLast('.', missingDelimiterValue = normalizedName)
        val language = subtitleLanguage(mediaStem, subtitleStem)
        if (subtitleStem != mediaStem && language == null) {
            return null
        }

        val mimeType = subtitleMimeTypeForName(normalizedName)
            ?: supportedSubtitleMimeType(mimeType)
            ?: return null

        return ExternalSubtitleCandidate(
            uri = uri,
            label = normalizedName,
            mimeType = mimeType,
            language = language
        )
    }

    private fun subtitleLanguage(mediaStem: String, subtitleStem: String): String? {
        val prefix = "$mediaStem."
        if (!subtitleStem.startsWith(prefix)) {
            return null
        }

        val suffix = subtitleStem.removePrefix(prefix)
        return suffix.takeIf { it.matches(languagePattern) }
    }

    private fun subtitleMimeTypeForName(name: String): String? {
        val extension = name.substringAfterLast('.', missingDelimiterValue = "")
            .lowercase()
        return subtitleMimeTypesByExtension[extension]
    }

    private fun supportedSubtitleMimeType(mimeType: String?): String? {
        val normalized = mimeType
            ?.substringBefore(';')
            ?.trim()
            ?.lowercase()
            .orEmpty()
        return normalized.takeIf { it in supportedSubtitleMimeTypes }
    }
}
