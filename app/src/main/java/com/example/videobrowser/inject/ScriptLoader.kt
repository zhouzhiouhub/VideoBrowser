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

    fun loadScript(path: String): String {
        val normalizedPath = validateScriptPath(path)
        return openAsset(normalizedPath).bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readText()
        }
    }

    fun loadCommonScript(): String {
        return loadScript(COMMON_SCRIPT_ASSET)
    }

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
        const val COMMON_SCRIPT_ASSET = "scripts/common.js"
        private const val SCRIPT_ASSET_DIRECTORY = "scripts/"
        private const val SCRIPT_EXTENSION = ".js"
    }
}
