package com.example.videobrowser

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
    val addressIcon: ImageView,
    val webViewContainer: FrameLayout,
    val webView: WebView,
    val addressInput: EditText,
    val pageProgress: ProgressBar,
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
        fun bind(activity: AppCompatActivity): MainActivityViews {
            return MainActivityViews(
                rootView = activity.findViewById(R.id.rootView),
                topBar = activity.findViewById(R.id.topBar),
                bottomBar = activity.findViewById(R.id.bottomBar),
                addressBar = activity.findViewById(R.id.addressBar),
                addressIcon = activity.findViewById(R.id.addressIcon),
                webViewContainer = activity.findViewById(R.id.webViewContainer),
                webView = activity.findViewById(R.id.webView),
                addressInput = activity.findViewById(R.id.addressInput),
                pageProgress = activity.findViewById(R.id.pageProgress),
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
