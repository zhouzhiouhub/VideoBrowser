package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 FunctionCenterActionGridLayout 可以拆开理解为“Function Center Action Grid Layout”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
object FunctionCenterActionGridLayout {
    private const val COLUMN_COUNT = 5

    /**
     * 函数 `rows`：封装 `rows` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param actionCount 参数类型为 `Int`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun rows(actionCount: Int): List<List<Int?>> {
        if (actionCount <= 0) {
            return emptyList()
        }

        return (0 until actionCount)
            .chunked(COLUMN_COUNT)
            .map { row ->
                row + List(COLUMN_COUNT - row.size) { null }
            }
    }
}
