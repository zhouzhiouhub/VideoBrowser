package com.example.videobrowser.adblock

data class SyntheticResponseSpec(
    val name: String,
    val mimeType: String,
    val encoding: String = DEFAULT_ENCODING,
    val statusCode: Int = HTTP_OK,
    val reasonPhrase: String = HTTP_OK_REASON,
    val body: ByteArray = ByteArray(0)
) {
    init {
        require(name.isNotBlank()) { "Synthetic response name must not be blank." }
        require(mimeType.isNotBlank()) { "Synthetic response MIME type must not be blank." }
        require(encoding.isNotBlank()) { "Synthetic response encoding must not be blank." }
        require(statusCode in 200..599) { "Synthetic response status code must be valid." }
        require(reasonPhrase.isNotBlank()) { "Synthetic response reason phrase must not be blank." }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SyntheticResponseSpec) return false

        return name == other.name &&
            mimeType == other.mimeType &&
            encoding == other.encoding &&
            statusCode == other.statusCode &&
            reasonPhrase == other.reasonPhrase &&
            body.contentEquals(other.body)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + encoding.hashCode()
        result = 31 * result + statusCode
        result = 31 * result + reasonPhrase.hashCode()
        result = 31 * result + body.contentHashCode()
        return result
    }

    companion object {
        const val DEFAULT_ENCODING = "utf-8"
        const val HTTP_OK = 200
        const val HTTP_OK_REASON = "OK"
    }
}
