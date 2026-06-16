package com.example.videobrowser.version

/**
 * 初学者阅读提示：
 * 这个文件属于“版本信息模块”。
 * 文件名 AppVersionFormatter 可以拆开理解为“App Version Formatter”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：把构建生成的版本号转换成界面上可展示的文本。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
object AppVersionFormatter {
    fun formatCommitCount(commitCount: Int): String {
        val count = commitCount.coerceAtLeast(0)
        val major = count / 1000
        val hundreds = (count / 100) % 10
        val tens = (count / 10) % 10
        val ones = count % 10
        return "$major.$hundreds.$tens.$ones"
    }
}
