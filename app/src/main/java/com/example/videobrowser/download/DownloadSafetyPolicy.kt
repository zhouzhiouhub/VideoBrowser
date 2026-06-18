package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadSafetyPolicy 可以拆开理解为“Download Safety Policy”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import com.example.videobrowser.utils.TextWhitespaceNormalizer
import java.net.URI
import java.util.Locale

/**
 * 下载前的安全判断。
 *
 * 这里不创建下载任务，只回答三个问题：是否需要确认、URL 是否可下载、文件名是否安全。
 */
object DownloadSafetyPolicy {
    private const val DEFAULT_DOWNLOAD_FILE_NAME = "download.bin"
    private const val MAX_DOWNLOAD_FILE_NAME_LENGTH = 120
    private val invalidDownloadFileNameChars = Regex("[\\\\/:*?\"<>|\\p{Cntrl}]")

    /**
     * 函数 `requiresConfirmation`：封装 `requires Confirmation` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param fileName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param mimeType 参数类型为 `String?`，表示函数执行 `mimeType` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun requiresConfirmation(fileName: String, mimeType: String?): Boolean {
        return DownloadCategory.from(mimeType, fileName) == DownloadCategory.APP
    }

    /**
     * 函数 `requiresInsecureTransportConfirmation`：封装 `requires Insecure Transport Confirmation` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param downloadUrl 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun requiresInsecureTransportConfirmation(pageUrl: String?, downloadUrl: String): Boolean {
        return schemeOf(pageUrl) == "https" && schemeOf(downloadUrl) == "http"
    }

    /**
     * 函数 `isDownloadableNetworkUrl`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isDownloadableNetworkUrl(url: String): Boolean {
        val uri = uriOf(url) ?: return false
        val scheme = uri.scheme?.lowercase(Locale.ROOT)
        return (scheme == "http" || scheme == "https") &&
            !uri.host.isNullOrBlank()
    }

    /**
     * 函数 `safeDownloadFileName`：封装 `safe Download File Name` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param fileName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun safeDownloadFileName(fileName: String): String {
        // 文件名会落到公共下载目录，必须移除路径分隔符、控制字符和 Windows 不允许的字符。
        val sanitized = TextWhitespaceNormalizer
            .collapse(fileName.trim().replace(invalidDownloadFileNameChars, "_"))
            .trim('.', ' ')
            .take(MAX_DOWNLOAD_FILE_NAME_LENGTH)
            .trim('.', ' ')
        return sanitized.ifBlank { DEFAULT_DOWNLOAD_FILE_NAME }
    }

    /**
     * 函数 `schemeOf`：封装 `scheme Of` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun schemeOf(url: String?): String? {
        return uriOf(url)
            ?.scheme
            ?.lowercase(Locale.ROOT)
    }

    /**
     * 函数 `uriOf`：封装 `uri Of` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun uriOf(url: String?): URI? {
        return runCatching { URI(url?.trim().orEmpty()) }.getOrNull()
    }
}
