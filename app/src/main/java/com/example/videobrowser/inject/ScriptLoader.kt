package com.example.videobrowser.inject

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
