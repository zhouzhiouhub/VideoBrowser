package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器调试开关模块”。
 * 文件名 BrowserWebViewDebugController 可以拆开理解为“Browser WebView Debug Controller”，
 * 表示它只负责根据应用构建标记开启 WebView 远程调试。
 * 阅读顺序：先看构造参数，再看 enableForDebuggableBuild() 如何判断 debug 包。
 */
import android.content.pm.ApplicationInfo
import android.webkit.WebView

/**
 * WebView 调试开关控制器。
 *
 * MainActivity 不需要知道 Android debug flag 的位运算细节；本类集中处理“只有 debug 包才打开
 * WebView DevTools”的系统级判断。
 *
 * @param applicationFlags 参数类型为 `Int`，表示 Android ApplicationInfo.flags，包含当前应用是否可调试等构建标记。
 */
class BrowserWebViewDebugController(
    private val applicationFlags: Int
) {
    /**
     * 在 debug 包中开启 WebView 远程调试。
     *
     * @return 无返回值；release 包不会调用 WebView.setWebContentsDebuggingEnabled(true)。
     */
    fun enableForDebuggableBuild() {
        if ((applicationFlags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }
}
