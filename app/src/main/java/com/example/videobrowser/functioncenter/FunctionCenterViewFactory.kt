package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 FunctionCenterViewFactory 可以拆开理解为“Function Center View Factory”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：构建底部功能面板、设置页面、数据管理页面以及各种用户可点击的工具入口。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.storage.SavedPage

/**
 * 功能中心 View 工厂。
 *
 * 项目没有使用 XML 编写这些弹层页面，而是在 Kotlin 中动态创建 View。
 * 把创建逻辑集中在这里，可以让页面类只描述“要显示哪些行和按钮”。
 */
class FunctionCenterViewFactory(
    private val activity: AppCompatActivity,
    private val dp: (Int) -> Int
) {
    private val surfaceFactory = FunctionCenterSurfaceFactory(activity, dp)
    private val pageFactory = FunctionCenterPageViewFactory(activity, dp, surfaceFactory)
    private val rowFactory = FunctionCenterRowFactory(activity, dp, surfaceFactory)
    private val gridFactory = FunctionCenterGridFactory(activity, dp)
    private val headerFactory = FunctionCenterHeaderFactory(activity, dp, surfaceFactory, rowFactory)
    private val contentFactory = FunctionCenterContentFactory(activity, dp, surfaceFactory, rowFactory)

    /**
     * 函数 `createPage`：创建 `create Page` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onBack 参数类型为 `() -> Unit`，表示函数执行 `onBack` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun createPage(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ): View {
        return pageFactory.createPage(title, onBack, buildContent)
    }

    /**
     * 函数 `createBottomSheetPage`：创建 `create Bottom Sheet Page` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param onBack 参数类型为 `(() -> Unit)?`，表示函数执行 `onBack` 相关逻辑时需要读取或处理的输入。
     * @param onClose 参数类型为 `() -> Unit`，表示函数执行 `onClose` 相关逻辑时需要读取或处理的输入。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun createBottomSheetPage(
        title: String,
        onBack: (() -> Unit)?,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ): View {
        return pageFactory.createBottomSheetPage(title, onBack, onClose, buildContent)
    }

    /**
     * 函数 `addProfileHeader`：封装 `add Profile Header` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param summary 参数类型为 `String`，表示函数执行 `summary` 相关逻辑时需要读取或处理的输入。
     * @param onClick 参数类型为 `() -> Unit`，表示函数执行 `onClick` 相关逻辑时需要读取或处理的输入。
     */
    fun addProfileHeader(
        parent: LinearLayout,
        title: String,
        summary: String,
        onClick: () -> Unit
    ) {
        headerFactory.addProfileHeader(parent, title, summary, onClick)
    }

    /**
     * 函数 `addBenefitStrip`：封装 `add Benefit Strip` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param leftTitle 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param leftSummary 参数类型为 `String`，表示函数执行 `leftSummary` 相关逻辑时需要读取或处理的输入。
     * @param rightTitle 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param rightSummary 参数类型为 `String`，表示函数执行 `rightSummary` 相关逻辑时需要读取或处理的输入。
     */
    fun addBenefitStrip(parent: LinearLayout, leftTitle: String, leftSummary: String, rightTitle: String, rightSummary: String) {
        headerFactory.addBenefitStrip(parent, leftTitle, leftSummary, rightTitle, rightSummary)
    }

    /**
     * 函数 `addHistoryPreview`：封装 `add History Preview` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param emptyMessage 参数类型为 `String`，表示函数执行 `emptyMessage` 相关逻辑时需要读取或处理的输入。
     * @param pages 参数类型为 `List<SavedPage>`，表示函数执行 `pages` 相关逻辑时需要读取或处理的输入。
     * @param onOpenPage 参数类型为 `(SavedPage) -> Unit`，表示函数执行 `onOpenPage` 相关逻辑时需要读取或处理的输入。
     * @param onShowHistory 参数类型为 `() -> Unit`，表示函数执行 `onShowHistory` 相关逻辑时需要读取或处理的输入。
     */
    fun addHistoryPreview(
        parent: LinearLayout,
        title: String,
        emptyMessage: String,
        pages: List<SavedPage>,
        onOpenPage: (SavedPage) -> Unit,
        onShowHistory: () -> Unit
    ) {
        headerFactory.addHistoryPreview(parent, title, emptyMessage, pages, onOpenPage, onShowHistory)
    }

    /**
     * 函数 `addFunctionSection`：封装 `add Function Section` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param buildContent 参数类型为 `(LinearLayout) -> Unit`，表示函数执行 `buildContent` 相关逻辑时需要读取或处理的输入。
     */
    fun addFunctionSection(
        parent: LinearLayout,
        title: String,
        buildContent: (LinearLayout) -> Unit
    ) {
        contentFactory.addFunctionSection(parent, title, buildContent)
    }

    /**
     * 函数 `addInfoRow`：封装 `add Info Row` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param summary 参数类型为 `String`，表示函数执行 `summary` 相关逻辑时需要读取或处理的输入。
     */
    fun addInfoRow(
        parent: LinearLayout,
        title: String,
        summary: String
    ) {
        contentFactory.addInfoRow(parent, title, summary)
    }

    /**
     * 函数 `addFunctionMessage`：封装 `add Function Message` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param message 参数类型为 `String`，表示函数执行 `message` 相关逻辑时需要读取或处理的输入。
     */
    fun addFunctionMessage(parent: LinearLayout, message: String) {
        contentFactory.addFunctionMessage(parent, message)
    }

    /**
     * 函数 `addEmptyState`：封装 `add Empty State` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param message 参数类型为 `String`，表示函数执行 `message` 相关逻辑时需要读取或处理的输入。
     */
    fun addEmptyState(parent: LinearLayout, message: String) {
        contentFactory.addEmptyState(parent, message)
    }

    /**
     * 函数 `addFunctionActionButton`：封装 `add Function Action Button` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param backgroundColor 参数类型为 `Int?`，表示函数执行 `backgroundColor` 相关逻辑时需要读取或处理的输入。
     * @param onClick 参数类型为 `() -> Unit`，表示函数执行 `onClick` 相关逻辑时需要读取或处理的输入。
     */
    fun addFunctionActionButton(
        parent: LinearLayout,
        title: String,
        backgroundColor: Int? = null,
        onClick: () -> Unit
    ) {
        contentFactory.addFunctionActionButton(parent, title, backgroundColor, onClick)
    }

    /**
     * 函数 `addActionGrid`：封装 `add Action Grid` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param actions 参数类型为 `List<FunctionCenterGridAction>`，表示函数执行 `actions` 相关逻辑时需要读取或处理的输入。
     */
    fun addActionGrid(
        parent: LinearLayout,
        actions: List<FunctionCenterGridAction>
    ) {
        gridFactory.addActionGrid(parent, actions)
    }

    /**
     * 函数 `addSwitchRow`：封装 `add Switch Row` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param summary 参数类型为 `String`，表示函数执行 `summary` 相关逻辑时需要读取或处理的输入。
     * @param checked 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     * @param onChanged 参数类型为 `(Boolean) -> Unit`，表示函数执行 `onChanged` 相关逻辑时需要读取或处理的输入。
     */
    fun addSwitchRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        checked: Boolean,
        enabled: Boolean = true,
        onChanged: (Boolean) -> Unit
    ) {
        contentFactory.addSwitchRow(parent, title, summary, checked, enabled, onChanged)
    }

    /**
     * 函数 `addActionRow`：封装 `add Action Row` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     * @param title 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param summary 参数类型为 `String`，表示函数执行 `summary` 相关逻辑时需要读取或处理的输入。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     * @param onClick 参数类型为 `() -> Unit`，表示函数执行 `onClick` 相关逻辑时需要读取或处理的输入。
     */
    fun addActionRow(
        parent: LinearLayout,
        title: String,
        summary: String,
        enabled: Boolean = true,
        onClick: () -> Unit
    ) {
        contentFactory.addActionRow(parent, title, summary, enabled, onClick)
    }

    /**
     * 函数 `addDivider`：封装 `add Divider` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param parent 参数类型为 `LinearLayout`，表示函数执行 `parent` 相关逻辑时需要读取或处理的输入。
     */
    fun addDivider(parent: LinearLayout) {
        contentFactory.addDivider(parent)
    }

}
