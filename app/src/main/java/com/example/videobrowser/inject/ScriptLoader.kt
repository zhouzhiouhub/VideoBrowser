package com.example.videobrowser.inject

/**
 * 初学者阅读提示：
 * 这个文件属于“页面脚本注入模块”。
 * 文件名 ScriptLoader 可以拆开理解为“Script Loader”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取内置 JavaScript，按当前站点和设置组合注入脚本，让页面净化和视频增强生效。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import android.content.res.AssetManager
import java.io.InputStream

/**
 * 只从应用内置 assets 加载 JavaScript 文件，避免 P7 阶段引入远程脚本来源。
 */
class ScriptLoader(
    private val openAsset: (String) -> InputStream
) {
    constructor(assets: AssetManager) : this({ path -> assets.open(path) })

    /**
     * 函数 `loadScript`：启动或加载 `load Script` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun loadScript(path: String): String {
        val normalizedPath = validateScriptPath(path)
        return openAsset(normalizedPath).bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readText()
        }
    }

    /**
     * 函数 `loadCommonScript`：启动或加载 `load Common Script` 对应的业务流程，通常会连接 UI、系统能力或网页状态。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun loadCommonScript(): String {
        return COMMON_SCRIPT_ASSETS.joinToString(separator = "\n\n") { path ->
            loadScript(path)
        }
    }

    /**
     * 函数 `validateScriptPath`：封装 `validate Script Path` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun validateScriptPath(path: String): String {
        val normalizedPath = path.trim()
        require(normalizedPath.isNotBlank()) {
            "Script asset path must not be blank."
        }
        require(normalizedPath.startsWith(SCRIPT_ASSET_DIRECTORY)) {
            "Script asset path must be under $SCRIPT_ASSET_DIRECTORY."
        }
        require(normalizedPath.endsWith(SCRIPT_EXTENSION)) {
            "Script asset path must point to a JavaScript file."
        }
        require(normalizedPath.none { it == '\\' } && normalizedPath.split('/').none { it == ".." || it.isBlank() }) {
            "Script asset path must be a relative assets path."
        }
        return normalizedPath
    }

    companion object {
        const val GEOMETRY_SCRIPT_ASSET = "scripts/geometry.js"
        const val DOM_TOOLS_SCRIPT_ASSET = "scripts/dom_tools.js"
        const val DOM_ACTIONS_SCRIPT_ASSET = "scripts/dom_actions.js"
        const val SELECTOR_TOOLS_SCRIPT_ASSET = "scripts/selector_tools.js"
        const val NATIVE_BRIDGE_SCRIPT_ASSET = "scripts/native_bridge.js"
        const val VIDEO_CONTROL_TOOLS_SCRIPT_ASSET = "scripts/video_control_tools.js"
        const val ELEMENT_PICKER_SCRIPT_ASSET = "scripts/element_picker.js"
        const val SCRIPTLET_HOOKS_SCRIPT_ASSET = "scripts/scriptlet_hooks.js"
        const val COMMON_SCRIPT_ASSET = "scripts/common.js"
        const val SITE_ADAPTER_HELPERS_SCRIPT_ASSET = "scripts/site_adapter_helpers.js"
        val COMMON_SCRIPT_ASSETS = listOf(
            GEOMETRY_SCRIPT_ASSET,
            DOM_TOOLS_SCRIPT_ASSET,
            DOM_ACTIONS_SCRIPT_ASSET,
            SELECTOR_TOOLS_SCRIPT_ASSET,
            NATIVE_BRIDGE_SCRIPT_ASSET,
            VIDEO_CONTROL_TOOLS_SCRIPT_ASSET,
            ELEMENT_PICKER_SCRIPT_ASSET,
            SCRIPTLET_HOOKS_SCRIPT_ASSET,
            COMMON_SCRIPT_ASSET
        )
        private const val SCRIPT_ASSET_DIRECTORY = "scripts/"
        private const val SCRIPT_EXTENSION = ".js"
    }
}
