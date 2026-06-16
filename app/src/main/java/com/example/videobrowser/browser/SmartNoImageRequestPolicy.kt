package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 SmartNoImageRequestPolicy 可以拆开理解为“Smart No Image Request Policy”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
object SmartNoImageRequestPolicy {
    /**
     * 函数 `shouldBlock`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     * @param siteSmartNoImageDisabled 参数类型为 `Boolean`，表示函数执行 `siteSmartNoImageDisabled` 相关逻辑时需要读取或处理的输入。
     * @param context 参数类型为 `RequestContext`，表示 Android 上下文，用来读取资源、启动系统服务或访问应用环境。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun shouldBlock(
        enabled: Boolean,
        siteSmartNoImageDisabled: Boolean = false,
        context: RequestContext
    ): Boolean {
        return enabled &&
            !siteSmartNoImageDisabled &&
            !context.isForMainFrame &&
            isHttpScheme(context.requestScheme) &&
            context.resourceType == ResourceType.IMAGE
    }

    /**
     * 函数 `isHttpScheme`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param scheme 参数类型为 `String?`，表示函数执行 `scheme` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun isHttpScheme(scheme: String?): Boolean {
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true)
    }
}
