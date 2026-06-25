package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 FunctionCenterPageHost 可以拆开理解为“Function Center Page Host”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class FunctionCenterPageHost(
    val activity: AppCompatActivity,
    private val functionCenter: FunctionCenterController
) {
    private val viewFactory = functionCenter.viewFactory
    internal val contentFactory = viewFactory.contentFactory
    internal val headerFactory = viewFactory.headerFactory
    internal val gridFactory = viewFactory.gridFactory

    /**
     * 函数 `showPage`：控制 `show Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onBack 参数类型为 `() -> Unit`，表示函数执行 `onBack` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     */
    fun showPage(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.showPage(title, onBack, buildContent)
    }

    /**
     * 函数 `showPage`：控制 `show Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onBack 参数类型为 `() -> Unit`，表示函数执行 `onBack` 相关逻辑时需要读取或处理的输入。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     */
    fun showPage(
        title: String,
        onBack: () -> Unit,
        replaceCurrent: Boolean,
        buildContent: (LinearLayout) -> Unit
    ) {
        if (replaceCurrent) {
            functionCenter.replacePage(title, onBack, buildContent)
        } else {
            functionCenter.showPage(title, onBack, buildContent)
        }
    }

    fun showPageWithFooter(
        title: String,
        onBack: () -> Unit,
        replaceCurrent: Boolean,
        buildContent: (LinearLayout) -> Unit,
        buildFooter: (LinearLayout) -> Unit
    ) {
        if (replaceCurrent) {
            functionCenter.replacePageWithFooter(title, onBack, buildContent, buildFooter)
        } else {
            functionCenter.showPageWithFooter(title, onBack, buildContent, buildFooter)
        }
    }

    /**
     * 函数 `replacePage`：封装 `replace Page` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onBack 参数类型为 `() -> Unit`，表示函数执行 `onBack` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     */
    fun replacePage(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.replacePage(title, onBack, buildContent)
    }

    /**
     * 函数 `showBottomSheetPage`：控制 `show Bottom Sheet Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onClose 参数类型为 `() -> Unit`，表示函数执行 `onClose` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     */
    fun showBottomSheetPage(
        title: String,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.showBottomSheetPage(title, onClose, buildContent)
    }

    /**
     * 函数 `showBottomSheetPage`：控制 `show Bottom Sheet Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onBack 参数类型为 `() -> Unit`，表示函数执行 `onBack` 相关逻辑时需要读取或处理的输入。
     * @param onClose 参数类型为 `() -> Unit`，表示函数执行 `onClose` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     */
    fun showBottomSheetPage(
        title: String,
        onBack: () -> Unit,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.showBottomSheetPage(title, onBack, onClose, buildContent)
    }

    /**
     * 函数 `showBottomSheetPage`：控制 `show Bottom Sheet Page` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onBack 参数类型为 `() -> Unit`，表示函数执行 `onBack` 相关逻辑时需要读取或处理的输入。
     * @param onClose 参数类型为 `() -> Unit`，表示函数执行 `onClose` 相关逻辑时需要读取或处理的输入。
     * @param replaceCurrent 参数类型为 `Boolean`，表示函数执行 `replaceCurrent` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     */
    fun showBottomSheetPage(
        title: String,
        onBack: () -> Unit,
        onClose: () -> Unit,
        replaceCurrent: Boolean,
        buildContent: (LinearLayout) -> Unit
    ) {
        if (replaceCurrent) {
            functionCenter.replaceBottomSheetPage(title, onBack, onClose, buildContent)
        } else {
            functionCenter.showBottomSheetPage(title, onBack, onClose, buildContent)
        }
    }

    /**
     * 函数 `replaceBottomSheetPage`：封装 `replace Bottom Sheet Page` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onBack 参数类型为 `() -> Unit`，表示函数执行 `onBack` 相关逻辑时需要读取或处理的输入。
     * @param onClose 参数类型为 `() -> Unit`，表示函数执行 `onClose` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     */
    fun replaceBottomSheetPage(
        title: String,
        onBack: () -> Unit,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ) {
        functionCenter.replaceBottomSheetPage(title, onBack, onClose, buildContent)
    }

    /**
     * 函数 `handleBack`：处理 `handle Back` 对应的事件或请求，集中完成校验、状态更新和回调通知。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun handleBack(): Boolean {
        return functionCenter.handleBack()
    }

    /**
     * 函数 `close`：控制 `close` 相关界面的显示、隐藏或关闭，并同步必要的界面状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun close(): Boolean {
        return functionCenter.close()
    }
}
