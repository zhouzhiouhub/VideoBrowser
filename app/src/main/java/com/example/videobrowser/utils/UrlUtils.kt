package com.example.videobrowser.utils

import java.net.IDN
import java.net.Inet6Address
import java.net.InetAddress
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

object UrlUtils {
    fun resolveAddressInput(
        input: String,
        searchUrlPrefix: String
    ): String? {
        val value = input.trim()
        if (value.isEmpty()) {
            return null
        }

        return resolveLoadableUrl(value)
            ?: "$searchUrlPrefix${encodeSearchQuery(value)}"
    }

    fun searchQueryFromUrl(url: String, searchUrlPrefix: String): String? {
        val prefixUri = parseUri(searchUrlPrefix) ?: return null
        val currentUri = parseUri(url) ?: return null
        if (!isSameSearchEndpoint(currentUri, prefixUri)) {
            return null
        }

        val queryParameterName = searchQueryParameterName(prefixUri) ?: return null
        val rawValue = rawQueryParameter(currentUri.rawQuery, queryParameterName) ?: return null
        return decodeFormComponent(rawValue)
            ?.replace(WHITESPACE_SEQUENCE, " ")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    fun displayUrl(url: String): String {
        val parsedUri = parseUri(url) ?: return decodePercentEncoded(url)
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

    private fun resolveLoadableUrl(value: String): String? {
        if (value.startsWith("about:", ignoreCase = true)) {
            return ABOUT_BLANK.takeIf { value.equals(it, ignoreCase = true) }
        }

        val schemeSeparator = value.indexOf("://")
        if (schemeSeparator > 0) {
            val scheme = normalizeScheme(value.take(schemeSeparator)) ?: return null
            if (scheme != HTTP_SCHEME && scheme != HTTPS_SCHEME) {
                return null
            }
            return normalizeNetworkUrl(value, defaultScheme = scheme, hasExplicitScheme = true)
        }

        if (value.startsWith("//")) {
            return normalizeNetworkUrl(
                value = value,
                defaultScheme = HTTPS_SCHEME,
                hasExplicitScheme = false
            )
        }

        return normalizeNetworkUrl(
            value = value,
            defaultScheme = if (looksLikeLocalOrIpAuthority(value)) HTTP_SCHEME else HTTPS_SCHEME,
            hasExplicitScheme = false
        )
    }

    private fun normalizeNetworkUrl(
        value: String,
        defaultScheme: String,
        hasExplicitScheme: Boolean
    ): String? {
        val withoutScheme = when {
            hasExplicitScheme -> value.substringAfter("://", missingDelimiterValue = "")
            value.startsWith("//") -> value.removePrefix("//")
            else -> value
        }

        val authorityEnd = withoutScheme.indexOfFirst { it == '/' || it == '?' || it == '#' }
            .takeIf { it >= 0 }
            ?: withoutScheme.length
        val authority = withoutScheme.take(authorityEnd)
        val tail = withoutScheme.substring(authorityEnd)
        val parsedAuthority = parseAuthority(authority, allowUserInfo = hasExplicitScheme)
            ?: return null

        if (!isLoadableHost(parsedAuthority.host)) {
            return null
        }

        val scheme = if (hasExplicitScheme) {
            normalizeScheme(value.substringBefore("://")) ?: return null
        } else {
            defaultScheme
        }
        return "$scheme://${parsedAuthority.render()}${encodeUrlTail(tail)}"
    }

    private fun parseAuthority(authority: String, allowUserInfo: Boolean): ParsedAuthority? {
        if (authority.isBlank() || authority.hasUnsafeCharacter()) {
            return null
        }

        val userInfoEnd = authority.lastIndexOf('@')
        if (userInfoEnd >= 0 && !allowUserInfo) {
            return null
        }
        val userInfo = userInfoEnd
            .takeIf { it >= 0 }
            ?.let { authority.take(it + 1) }
            .orEmpty()
        val hostAndPort = if (userInfoEnd >= 0) authority.substring(userInfoEnd + 1) else authority
        if (hostAndPort.isEmpty()) {
            return null
        }

        return if (hostAndPort.startsWith("[")) {
            parseBracketedIpv6Authority(userInfo, hostAndPort)
        } else {
            parseNamedOrIpv4Authority(userInfo, hostAndPort)
                ?: parseNakedIpv6Authority(userInfo, hostAndPort)
        }
    }

    private fun parseBracketedIpv6Authority(
        userInfo: String,
        hostAndPort: String
    ): ParsedAuthority? {
        val closeBracket = hostAndPort.indexOf(']')
        if (closeBracket <= 1) {
            return null
        }

        val host = hostAndPort.substring(1, closeBracket).lowercase(Locale.ROOT)
        if (!isIpv6Address(host)) {
            return null
        }

        val port = parsePortSuffix(hostAndPort.substring(closeBracket + 1)) ?: return null
        return ParsedAuthority(
            userInfo = userInfo,
            host = host,
            port = port,
            bracketIpv6 = true
        )
    }

    private fun parseNamedOrIpv4Authority(
        userInfo: String,
        hostAndPort: String
    ): ParsedAuthority? {
        if (hostAndPort.count { it == ':' } > 1) {
            return null
        }

        val portSeparator = hostAndPort.lastIndexOf(':').takeIf { it >= 0 }
        val rawHost = portSeparator?.let { hostAndPort.take(it) } ?: hostAndPort
        if (rawHost.isEmpty()) {
            return null
        }
        val port = portSeparator?.let { parsePort(hostAndPort.substring(it + 1)) } ?: NO_PORT
        if (port == INVALID_PORT) {
            return null
        }

        val host = normalizeHost(rawHost) ?: return null
        return ParsedAuthority(
            userInfo = userInfo,
            host = host,
            port = port,
            bracketIpv6 = false
        )
    }

    private fun parseNakedIpv6Authority(
        userInfo: String,
        hostAndPort: String
    ): ParsedAuthority? {
        val host = hostAndPort.trimEnd('.').lowercase(Locale.ROOT)
        if (!isIpv6Address(host)) {
            return null
        }
        return ParsedAuthority(
            userInfo = userInfo,
            host = host,
            port = NO_PORT,
            bracketIpv6 = true
        )
    }

    private fun parsePortSuffix(suffix: String): Int? {
        if (suffix.isEmpty()) {
            return NO_PORT
        }
        if (!suffix.startsWith(":")) {
            return null
        }
        val port = parsePort(suffix.drop(1))
        return port.takeUnless { it == INVALID_PORT }
    }

    private fun parsePort(value: String): Int {
        if (value.isEmpty() || value.any { !it.isDigit() }) {
            return INVALID_PORT
        }
        return value.toIntOrNull()?.takeIf { it in 0..65535 } ?: INVALID_PORT
    }

    private fun normalizeHost(rawHost: String): String? {
        val trimmedHost = rawHost.trimEnd('.')
        if (trimmedHost.isEmpty() || trimmedHost.hasUnsafeCharacter()) {
            return null
        }

        return runCatching {
            IDN.toASCII(trimmedHost.lowercase(Locale.ROOT))
                .trimEnd('.')
                .lowercase(Locale.ROOT)
        }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    private fun isLoadableHost(host: String): Boolean {
        return isLocalhost(host) ||
            isIpv4Address(host) ||
            isIpv6Address(host) ||
            isDomainName(host)
    }

    private fun looksLikeLocalOrIpAuthority(value: String): Boolean {
        val authorityEnd = value.indexOfFirst { it == '/' || it == '?' || it == '#' }
            .takeIf { it >= 0 }
            ?: value.length
        val authority = value.take(authorityEnd)
        val parsedAuthority = parseAuthority(authority, allowUserInfo = false) ?: return false
        return isLocalhost(parsedAuthority.host) ||
            isIpv4Address(parsedAuthority.host) ||
            isIpv6Address(parsedAuthority.host)
    }

    private fun isLocalhost(host: String): Boolean {
        return host == LOCALHOST || host == ANDROID_EMULATOR_HOST
    }

    private fun isIpv4Address(host: String): Boolean {
        val parts = host.split(".")
        return parts.size == 4 && parts.all { part ->
            part.isNotEmpty() &&
                part.all { it.isDigit() } &&
                part.toIntOrNull()?.let { it in 0..255 } == true
        }
    }

    private fun isIpv6Address(host: String): Boolean {
        if (!host.contains(":")) {
            return false
        }
        return runCatching { InetAddress.getByName(host) }
            .getOrNull() is Inet6Address
    }

    private fun isDomainName(host: String): Boolean {
        if (!host.contains(".") || !host.any { it.isLetter() }) {
            return false
        }

        return host.split(".").all { label ->
            label.isNotEmpty() &&
                label.length <= MAX_DOMAIN_LABEL_LENGTH &&
                label.all { it.isLetterOrDigit() || it == '-' || it == '_' } &&
                !label.startsWith("-") &&
                !label.endsWith("-")
        }
    }

    private fun normalizeScheme(scheme: String): String? {
        return scheme
            .takeIf { it.first().isLetter() }
            ?.takeIf { candidate ->
                candidate.all { it.isLetterOrDigit() || it == '+' || it == '-' || it == '.' }
            }
            ?.lowercase(Locale.ROOT)
    }

    private fun encodeSearchQuery(value: String): String {
        val query = value.replace(WHITESPACE_SEQUENCE, " ").trim()
        return URLEncoder.encode(query, StandardCharsets.UTF_8.name())
    }

    private fun parseUri(value: String): URI? {
        return runCatching { URI(value.trim()) }.getOrNull()
    }

    private fun isSameSearchEndpoint(currentUri: URI, prefixUri: URI): Boolean {
        return currentUri.scheme.equals(prefixUri.scheme, ignoreCase = true) &&
            currentUri.host.equals(prefixUri.host, ignoreCase = true) &&
            normalizedUriPath(currentUri) == normalizedUriPath(prefixUri)
    }

    private fun normalizedUriPath(uri: URI): String {
        return uri.rawPath.orEmpty().ifEmpty { "/" }.trimEnd('/')
    }

    private fun searchQueryParameterName(prefixUri: URI): String? {
        val rawQuery = prefixUri.rawQuery ?: return null
        return rawQuery
            .split("&")
            .lastOrNull()
            ?.substringBefore("=")
            ?.takeIf { it.isNotBlank() }
            ?.let { decodeFormComponent(it) }
    }

    private fun rawQueryParameter(rawQuery: String?, queryParameterName: String): String? {
        return rawQuery
            ?.split("&")
            ?.firstNotNullOfOrNull { part ->
                val rawName = part.substringBefore("=")
                val decodedName = decodeFormComponent(rawName)
                if (decodedName == queryParameterName && part.contains("=")) {
                    part.substringAfter("=")
                } else {
                    null
                }
            }
    }

    private fun decodeFormComponent(value: String): String? {
        return runCatching {
            URLDecoder.decode(value, StandardCharsets.UTF_8.name())
        }.getOrNull()
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

    private fun encodeUrlTail(value: String): String {
        if (value.isEmpty()) {
            return value
        }

        return buildString {
            value.forEach { char ->
                when {
                    char == ' ' -> append("%20")
                    char == '\t' || char == '\n' || char == '\r' -> Unit
                    char.isISOControl() || char.isUnsafeUrlTailCharacter() -> appendUtf8PercentEncoded(char)
                    else -> append(char)
                }
            }
        }
    }

    private fun StringBuilder.appendUtf8PercentEncoded(char: Char) {
        char.toString().toByteArray(StandardCharsets.UTF_8).forEach { byte ->
            append('%')
            append(HEX_DIGITS[(byte.toInt() shr 4) and 0xF])
            append(HEX_DIGITS[byte.toInt() and 0xF])
        }
    }

    private fun Char.isUnsafeUrlTailCharacter(): Boolean {
        return this == '"' ||
            this == '<' ||
            this == '>' ||
            this == '\\' ||
            this == '^' ||
            this == '`' ||
            this == '{' ||
            this == '|' ||
            this == '}'
    }

    private fun String.hasUnsafeCharacter(): Boolean {
        return any { it.isWhitespace() || it.isISOControl() }
    }

    private data class ParsedAuthority(
        val userInfo: String,
        val host: String,
        val port: Int,
        val bracketIpv6: Boolean
    ) {
        fun render(): String {
            val renderedHost = if (bracketIpv6) "[$host]" else host
            val renderedPort = port.takeUnless { it == NO_PORT }?.let { ":$it" }.orEmpty()
            return "$userInfo$renderedHost$renderedPort"
        }
    }

    private const val HTTP_SCHEME = "http"
    private const val HTTPS_SCHEME = "https"
    private const val ABOUT_BLANK = "about:blank"
    private const val LOCALHOST = "localhost"
    private const val ANDROID_EMULATOR_HOST = "10.0.2.2"
    private const val MAX_DOMAIN_LABEL_LENGTH = 63
    private const val NO_PORT = -1
    private const val INVALID_PORT = -2
    private const val HEX_DIGITS = "0123456789ABCDEF"
    private val WHITESPACE_SEQUENCE = Regex("\\s+")
}
