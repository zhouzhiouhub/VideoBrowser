package com.example.videobrowser.utils

/**
 * 初学者阅读提示：
 * 这个文件属于“通用工具模块”。
 * 文件名 UrlUtils 可以拆开理解为“Url Utils”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：提供 URL、媒体地址等跨模块复用的纯函数。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import java.net.IDN
import java.net.Inet6Address
import java.net.InetAddress
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * URL 解析和展示工具。
 *
 * 这里的函数都是纯函数：不访问 Android 系统，也不读写状态。
 * 地址栏、搜索建议、站点安全和历史展示都会复用这些规则。
 */
object UrlUtils {
    /**
     * 函数 `resolveAddressInput`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param input 参数类型为 `String`，表示函数执行 `input` 相关逻辑时需要读取或处理的输入。
     * @param searchUrlPrefix 参数类型为 `String`，表示函数执行 `searchUrlPrefix` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun resolveAddressInput(
        input: String,
        searchUrlPrefix: String
    ): String? {
        // 地址栏输入如果能解析成网页 URL 就直接打开，否则拼成当前搜索引擎的搜索 URL。
        val value = input.trim()
        if (value.isEmpty()) {
            return null
        }

        return resolveLoadableUrl(value)
            ?: "$searchUrlPrefix${encodeSearchQuery(value)}"
    }

    /**
     * 函数 `searchQueryFromUrl`：封装 `search Query From Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param searchUrlPrefix 参数类型为 `String`，表示函数执行 `searchUrlPrefix` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun searchQueryFromUrl(url: String, searchUrlPrefix: String): String? {
        return SearchUrlQueryParser.searchQueryFromUrl(url, searchUrlPrefix)
    }

    /**
     * 函数 `displayUrl`：封装 `display Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `resolveLoadableUrl`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `normalizeNetworkUrl`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param defaultScheme 参数类型为 `String`，表示函数执行 `defaultScheme` 相关逻辑时需要读取或处理的输入。
     * @param hasExplicitScheme 参数类型为 `Boolean`，表示函数执行 `hasExplicitScheme` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeNetworkUrl(
        value: String,
        defaultScheme: String,
        hasExplicitScheme: Boolean
    ): String? {
        // 这里会补齐 scheme、校验 host、保留 path/query/fragment，并对尾部做必要编码。
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

    /**
     * 函数 `parseAuthority`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param authority 参数类型为 `String`，表示函数执行 `authority` 相关逻辑时需要读取或处理的输入。
     * @param allowUserInfo 参数类型为 `Boolean`，表示函数执行 `allowUserInfo` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `parseBracketedIpv6Authority`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param userInfo 参数类型为 `String`，表示函数执行 `userInfo` 相关逻辑时需要读取或处理的输入。
     * @param hostAndPort 参数类型为 `String`，表示函数执行 `hostAndPort` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `parseNamedOrIpv4Authority`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param userInfo 参数类型为 `String`，表示函数执行 `userInfo` 相关逻辑时需要读取或处理的输入。
     * @param hostAndPort 参数类型为 `String`，表示函数执行 `hostAndPort` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `parseNakedIpv6Authority`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param userInfo 参数类型为 `String`，表示函数执行 `userInfo` 相关逻辑时需要读取或处理的输入。
     * @param hostAndPort 参数类型为 `String`，表示函数执行 `hostAndPort` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `parsePortSuffix`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param suffix 参数类型为 `String`，表示函数执行 `suffix` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `parsePort`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun parsePort(value: String): Int {
        if (value.isEmpty() || value.any { !it.isDigit() }) {
            return INVALID_PORT
        }
        return value.toIntOrNull()?.takeIf { it in 0..65535 } ?: INVALID_PORT
    }

    /**
     * 函数 `normalizeHost`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rawHost 参数类型为 `String`，表示函数执行 `rawHost` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `isLoadableHost`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isLoadableHost(host: String): Boolean {
        return isLocalhost(host) ||
            isIpv4Address(host) ||
            isIpv6Address(host) ||
            isDomainName(host)
    }

    /**
     * 函数 `looksLikeLocalOrIpAuthority`：封装 `looks Like Local Or Ip Authority` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `isLocalhost`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isLocalhost(host: String): Boolean {
        return host == LOCALHOST || host == ANDROID_EMULATOR_HOST
    }

    /**
     * 函数 `isIpv4Address`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isIpv4Address(host: String): Boolean {
        val parts = host.split(".")
        return parts.size == 4 && parts.all { part ->
            part.isNotEmpty() &&
                part.all { it.isDigit() } &&
                part.toIntOrNull()?.let { it in 0..255 } == true
        }
    }

    /**
     * 函数 `isIpv6Address`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isIpv6Address(host: String): Boolean {
        if (!host.contains(":")) {
            return false
        }
        return runCatching { InetAddress.getByName(host) }
            .getOrNull() is Inet6Address
    }

    /**
     * 函数 `isDomainName`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param host 参数类型为 `String`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `normalizeScheme`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param scheme 参数类型为 `String`，表示函数执行 `scheme` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun normalizeScheme(scheme: String): String? {
        return scheme
            .takeIf { it.first().isLetter() }
            ?.takeIf { candidate ->
                candidate.all { it.isLetterOrDigit() || it == '+' || it == '-' || it == '.' }
            }
            ?.lowercase(Locale.ROOT)
    }

    /**
     * 函数 `encodeSearchQuery`：封装 `encode Search Query` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun encodeSearchQuery(value: String): String {
        val query = value.replace(WHITESPACE_SEQUENCE, " ").trim()
        return URLEncoder.encode(query, StandardCharsets.UTF_8.name())
    }

    /**
     * 函数 `parseUri`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun parseUri(value: String): URI? {
        return runCatching { URI(value.trim()) }.getOrNull()
    }

    /**
     * 函数 `decodePercentEncoded`：封装 `decode Percent Encoded` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `hasHexByteAt`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @param index 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun hasHexByteAt(value: String, index: Int): Boolean {
        return index + 2 < value.length &&
            hexValue(value[index + 1]) >= 0 &&
            hexValue(value[index + 2]) >= 0
    }

    /**
     * 函数 `hexValue`：封装 `hex Value` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param char 参数类型为 `Char`，表示函数执行 `char` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun hexValue(char: Char): Int {
        return when (char) {
            in '0'..'9' -> char - '0'
            in 'a'..'f' -> char - 'a' + 10
            in 'A'..'F' -> char - 'A' + 10
            else -> -1
        }
    }

    /**
     * 函数 `encodeUrlTail`：封装 `encode Url Tail` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `appendUtf8PercentEncoded`：封装 `append Utf8 Percent Encoded` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param char 参数类型为 `Char`，表示函数执行 `char` 相关逻辑时需要读取或处理的输入。
     */
    private fun StringBuilder.appendUtf8PercentEncoded(char: Char) {
        char.toString().toByteArray(StandardCharsets.UTF_8).forEach { byte ->
            append('%')
            append(HEX_DIGITS[(byte.toInt() shr 4) and 0xF])
            append(HEX_DIGITS[byte.toInt() and 0xF])
        }
    }

    /**
     * 函数 `isUnsafeUrlTailCharacter`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `hasUnsafeCharacter`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun String.hasUnsafeCharacter(): Boolean {
        return any { it.isWhitespace() || it.isISOControl() }
    }

    private data class ParsedAuthority(
        val userInfo: String,
        val host: String,
        val port: Int,
        val bracketIpv6: Boolean
    ) {
        /**
         * 函数 `render`：封装 `render` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
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
