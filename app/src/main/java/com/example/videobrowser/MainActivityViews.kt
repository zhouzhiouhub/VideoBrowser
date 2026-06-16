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
    val loadButton: ImageButton,
    val fullscreenContainer: FrameLayout
) {
    companion object {
        /**
         * 从 Activity 当前加载的布局中找到所有主界面控件。
         *
         * 这里要求调用方已经执行过 setContentView(R.layout.activity_main)，
         * 否则 findViewById 找不到对应控件，应用会在启动阶段暴露布局问题。
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
