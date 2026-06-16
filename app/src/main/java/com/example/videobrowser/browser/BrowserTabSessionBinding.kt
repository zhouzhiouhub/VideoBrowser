package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserTabSessionBinding 可以拆开理解为“Browser Tab Session Binding”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
class BrowserTabSessionBinding(
    private val tabs: BrowserTabStore
) {
    /**
     * 函数 `handlePageMetadataChanged`：处理 `handle Page Metadata Changed` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param title 参数类型为 `String?`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     */
    fun handlePageMetadataChanged(url: String?, title: String?) {
        tabs.updateActiveTab(
            url = url,
            title = title ?: tabs.activeTab().title
        )
    }
}
