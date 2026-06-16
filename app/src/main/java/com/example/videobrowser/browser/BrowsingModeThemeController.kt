package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览模式主题模块”。
 * 普通模式和无痕模式会使用不同颜色，本类负责把这些颜色应用到 MainActivity 的控件上。
 * 主要职责：隐藏无痕标记、刷新根背景/工具栏/地址栏/进度条/图标颜色，并同步状态栏明暗外观。
 * 阅读顺序：先看 updatePrivateBrowsingUi，再看 applyBrowsingModeTheme。
 */
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import com.example.videobrowser.MainActivityBrowsingModeTheme
import com.example.videobrowser.MainActivityViews

/**
 * 浏览模式主题控制器。
 *
 * MainActivity 只负责在模式变化或主页显示状态变化时调用本类；本类负责具体控件颜色应用。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来读取颜色资源和控制状态栏外观。
 * @param views 参数类型为 `MainActivityViews`，表示主界面的所有常用控件引用。
 * @param isPrivateBrowsingEnabled 参数类型为 `() -> Boolean`，表示读取当前是否处于无痕模式的回调。
 * @param currentPageUrl 参数类型为 `() -> String?`，表示读取当前页面 URL 的回调，用来刷新站点安全图标。
 * @param updateSiteSecurityStatus 参数类型为 `(String?) -> Unit`，表示通知站点安全模块刷新 URL 状态的回调。
 * @param dp 参数类型为 `(Int) -> Int`，表示把 dp 单位换算为像素的回调，用来设置地址栏圆角和描边。
 */
class BrowsingModeThemeController(
    private val activity: AppCompatActivity,
    private val views: MainActivityViews,
    private val isPrivateBrowsingEnabled: () -> Boolean,
    private val currentPageUrl: () -> String?,
    private val updateSiteSecurityStatus: (String?) -> Unit,
    private val dp: (Int) -> Int
) {
    /**
     * 函数 `updatePrivateBrowsingUi`：刷新无痕模式相关 UI。
     *
     * 初学者阅读提示：当前设计隐藏文字徽标，只通过整体深色主题表达无痕模式。
     */
    fun updatePrivateBrowsingUi() {
        views.privateBrowsingBadge.visibility = View.GONE
        applyBrowsingModeTheme()
    }

    /**
     * 函数 `applyBrowsingModeTheme`：把普通/无痕模式颜色应用到所有主界面控件。
     */
    fun applyBrowsingModeTheme() {
        val colors = MainActivityBrowsingModeTheme.colors(
            context = activity,
            privateBrowsing = isPrivateBrowsingEnabled()
        )

        views.rootView.setBackgroundColor(colors.background)
        views.topBar.setBackgroundColor(colors.surface)
        views.bottomBar.setBackgroundColor(colors.surface)
        views.searchProviderScroll.setBackgroundColor(colors.background)
        views.addressSuggestionPanel.setBackgroundColor(colors.surface)
        views.webViewContainer.setBackgroundColor(colors.webViewBackground)
        views.addressInput.setTextColor(colors.text)
        views.addressInput.setHintTextColor(colors.hint)
        views.addressBar.background = GradientDrawable().apply {
            cornerRadius = dp(22).toFloat()
            setColor(colors.addressBackground)
            setStroke(dp(1), colors.addressStroke)
        }
        updateSiteSecurityStatus(currentPageUrl())
        listOf(
            views.backButton,
            views.refreshButton,
            views.pageToolsButton,
            views.bookmarkButton,
            views.wenxinButton,
            views.profileButton
        ).forEach { button ->
            button.setColorFilter(colors.icon)
        }
        views.pageProgress.progressTintList = ColorStateList.valueOf(colors.progress)
        WindowInsetsControllerCompat(activity.window, views.rootView).isAppearanceLightStatusBars =
            !isPrivateBrowsingEnabled()
    }
}
