package com.example.videobrowser

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat

/**
 * 主界面浏览模式主题模块。
 *
 * MainActivity 需要根据“普通模式/无痕模式”切换一整组界面颜色。
 * 这个文件把“选择颜色”的逻辑从 Activity 里拆出来，让 Activity 只保留“把颜色应用到控件”的职责。
 *
 * 初学者可以这样理解：
 * - BrowserUiColors 是一个装颜色的盒子。
 * - MainActivityBrowsingModeTheme.colors() 会根据 privateBrowsing 决定盒子里放哪些颜色。
 * - MainActivity.applyBrowsingModeTheme() 再把这些颜色设置到地址栏、底栏、网页容器等控件上。
 */
internal object MainActivityBrowsingModeTheme {

    fun colors(context: Context, privateBrowsing: Boolean): BrowserUiColors {
        return if (privateBrowsing) {
            privateBrowsingColors()
        } else {
            normalBrowsingColors(context)
        }
    }

    /**
     * 无痕模式颜色直接写在代码里，因为它是一套只服务无痕浏览的深色临时外观。
     */
    private fun privateBrowsingColors(): BrowserUiColors {
        return BrowserUiColors(
            background = Color.parseColor("#11151B"),
            surface = Color.parseColor("#181D25"),
            webViewBackground = Color.parseColor("#0B0F14"),
            addressBackground = Color.parseColor("#222936"),
            addressStroke = Color.parseColor("#303948"),
            text = Color.parseColor("#F4F7FB"),
            hint = Color.parseColor("#8F9BAD"),
            icon = Color.parseColor("#E9EEF7"),
            mutedIcon = Color.parseColor("#AAB4C3"),
            progress = Color.parseColor("#4D8DFF")
        )
    }

    /**
     * 普通模式复用 res/values/colors.xml 里的资源，方便以后统一换肤或适配主题。
     */
    private fun normalBrowsingColors(context: Context): BrowserUiColors {
        return BrowserUiColors(
            background = ContextCompat.getColor(context, R.color.browser_background),
            surface = ContextCompat.getColor(context, R.color.browser_surface),
            webViewBackground = ContextCompat.getColor(context, R.color.webview_background),
            addressBackground = ContextCompat.getColor(context, R.color.address_bar_background),
            addressStroke = ContextCompat.getColor(context, R.color.address_bar_stroke),
            text = ContextCompat.getColor(context, R.color.browser_text),
            hint = ContextCompat.getColor(context, R.color.browser_text_hint),
            icon = ContextCompat.getColor(context, R.color.browser_icon),
            mutedIcon = ContextCompat.getColor(context, R.color.browser_icon_muted),
            progress = ContextCompat.getColor(context, R.color.progress_active)
        )
    }
}

/**
 * 主界面在普通/无痕模式下使用的一组颜色。
 *
 * 单独建成 data class 是为了让代码先“决定颜色”，再“应用颜色”，
 * 初学者阅读时可以把两步分开理解。
 */
internal data class BrowserUiColors(
    val background: Int,
    val surface: Int,
    val webViewBackground: Int,
    val addressBackground: Int,
    val addressStroke: Int,
    val text: Int,
    val hint: Int,
    val icon: Int,
    val mutedIcon: Int,
    val progress: Int
)
