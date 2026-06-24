package com.example.videobrowser

/**
 * MainActivity 的视图绑定清单。
 *
 * 初学者阅读提示：
 * 可以把这个类理解为“activity_main.xml 里常用控件的索引表”：
 * - 左边的属性名是 Kotlin 代码里要使用的变量。
 * - 右边的 findViewById 会按 XML 里的 id 找到真实控件。
 * - MainActivity 只保留一个 views 对象，避免在主类里散落大量 findViewById。
 */
import android.view.View
import android.webkit.WebView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * 主界面视图引用集合。
 *
 * 这个 data class 只保存布局控件引用，不处理业务逻辑；MainActivity 和各装配类通过它传递 UI 控件。
 *
 * @param rootView 参数类型为 `View`，表示 activity_main.xml 的根视图，用于全局布局监听和启动阶段 UI 初始化。
 * @param topBar 参数类型为 `View`，表示顶部地址栏区域的容器视图。
 * @param bottomBar 参数类型为 `ConstraintLayout`，表示底部浏览操作按钮栏。
 * @param addressBar 参数类型为 `LinearLayout`，表示包含地址输入和站点安全提示的地址栏容器。
 * @param addressProviderBadge 参数类型为 `TextView`，表示当前搜索提供方的文字标识。
 * @param siteSecurityIcon 参数类型为 `ImageView`，表示站点安全状态图标。
 * @param webViewContainer 参数类型为 `FrameLayout`，表示承载标准/无痕 WebView 的父容器。
 * @param webView 参数类型为 `WebView`，表示布局中初始声明的标准浏览 WebView。
 * @param addressInput 参数类型为 `EditText`，表示地址栏输入框。
 * @param pageProgress 参数类型为 `ProgressBar`，表示页面加载进度条。
 * @param addressSuggestionPanel 参数类型为 `LinearLayout`，表示地址输入建议列表容器。
 * @param searchProviderScroll 参数类型为 `HorizontalScrollView`，表示搜索提供方横向滚动区域。
 * @param searchProviderList 参数类型为 `LinearLayout`，表示搜索提供方按钮列表容器。
 * @param privateBrowsingBadge 参数类型为 `TextView`，表示无痕浏览模式提示标识。
 * @param pageToolsButton 参数类型为 `ImageButton`，表示打开页面工具面板的按钮。
 * @param wenxinButton 参数类型为 `ImageButton`，表示打开文心相关入口的按钮。
 * @param profileButton 参数类型为 `ImageButton`，表示打开个人/功能中心入口的按钮。
 * @param backButton 参数类型为 `ImageButton`，表示浏览器后退按钮。
 * @param refreshButton 参数类型为 `ImageButton`，表示刷新或停止加载按钮。
 * @param bookmarkButton 参数类型为 `ImageButton`，表示收藏当前页面的按钮。
 * @param loadButton 参数类型为 `TextView`，表示地址栏加载或跳转按钮。
 * @param fullscreenContainer 参数类型为 `FrameLayout`，表示视频或页面全屏内容的承载容器。
 */
data class MainActivityViews(
    val rootView: View,
    val topBar: View,
    val bottomBar: ConstraintLayout,
    val addressBar: LinearLayout,
    val addressProviderBadge: TextView,
    val siteSecurityIcon: ImageView,
    val webViewContainer: FrameLayout,
    val webView: WebView,
    val addressInput: EditText,
    val pageProgress: ProgressBar,
    val addressSuggestionPanel: LinearLayout,
    val searchProviderScroll: HorizontalScrollView,
    val searchProviderList: LinearLayout,
    val privateBrowsingBadge: TextView,
    val pageToolsButton: ImageButton,
    val wenxinButton: ImageButton,
    val profileButton: ImageButton,
    val backButton: ImageButton,
    val refreshButton: ImageButton,
    val bookmarkButton: ImageButton,
    val loadButton: TextView,
    val fullscreenContainer: FrameLayout
) {
    companion object {
        /**
         * 从 Activity 当前加载的布局中找到所有主界面控件。
         *
         * 这里要求调用方已经执行过 setContentView(R.layout.activity_main)，
         * 否则 findViewById 找不到对应控件，应用会在启动阶段暴露布局问题。
         *
         * @param activity 参数类型为 `AppCompatActivity`，表示已经加载 activity_main.xml 的宿主 Activity。
         * @return 返回 `MainActivityViews`，其中每个字段都对应一个从布局中找到的控件引用。
         */
        fun bind(activity: AppCompatActivity): MainActivityViews {
            return MainActivityViews(
                rootView = activity.findViewById(R.id.rootView),
                topBar = activity.findViewById(R.id.topBar),
                bottomBar = activity.findViewById(R.id.bottomBar),
                addressBar = activity.findViewById(R.id.addressBar),
                addressProviderBadge = activity.findViewById(R.id.addressProviderBadge),
                siteSecurityIcon = activity.findViewById(R.id.siteSecurityIcon),
                webViewContainer = activity.findViewById(R.id.webViewContainer),
                webView = activity.findViewById(R.id.webView),
                addressInput = activity.findViewById(R.id.addressInput),
                pageProgress = activity.findViewById(R.id.pageProgress),
                addressSuggestionPanel = activity.findViewById(R.id.addressSuggestionPanel),
                searchProviderScroll = activity.findViewById(R.id.searchProviderScroll),
                searchProviderList = activity.findViewById(R.id.searchProviderList),
                privateBrowsingBadge = activity.findViewById(R.id.privateBrowsingBadge),
                pageToolsButton = activity.findViewById(R.id.pageToolsButton),
                wenxinButton = activity.findViewById(R.id.wenxinButton),
                profileButton = activity.findViewById(R.id.profileButton),
                backButton = activity.findViewById(R.id.backButton),
                refreshButton = activity.findViewById(R.id.refreshButton),
                bookmarkButton = activity.findViewById(R.id.bookmarkButton),
                loadButton = activity.findViewById(R.id.loadButton),
                fullscreenContainer = activity.findViewById(R.id.fullscreenContainer)
            )
        }
    }
}
