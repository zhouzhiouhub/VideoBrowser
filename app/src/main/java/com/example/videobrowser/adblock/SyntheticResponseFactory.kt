package com.example.videobrowser.adblock

import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

class SyntheticResponseFactory(
    private val registry: SyntheticResponseRegistry = SyntheticResponseRegistry()
) {
    fun create(resourceName: String?): WebResourceResponse? {
        val spec = registry.get(resourceName) ?: return null
        return WebResourceResponse(
            spec.mimeType,
            spec.encoding,
            ByteArrayInputStream(spec.body)
        ).apply {
            setStatusCodeAndReasonPhrase(spec.statusCode, spec.reasonPhrase)
        }
    }
}
