package com.example.videobrowser.adblock

/**
 * 初学者阅读提示：
 * 这个文件属于“广告请求拦截模块”。
 * 文件名 SyntheticResponseSpec 可以拆开理解为“Synthetic Response Spec”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：把 WebView 的网络请求交给规则系统判断，并在需要拦截时返回安全的本地响应。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
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

    /**
     * 函数 `equals`：封装 `equals` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param other 参数类型为 `Any?`，表示函数执行 `other` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `hashCode`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
