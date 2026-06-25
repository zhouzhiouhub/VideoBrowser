package com.example.videobrowser.functioncenter

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videobrowser.R

internal class FunctionCenterPageViewFactory(
    private val activity: AppCompatActivity,
    private val dp: (Int) -> Int,
    private val surfaceFactory: FunctionCenterSurfaceFactory
) {
    fun createPage(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ): View {
        return createPageSurface(title, onBack, buildContent, buildFooter = null)
    }

    fun createPageWithFooter(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit,
        buildFooter: (LinearLayout) -> Unit
    ): View {
        return createPageSurface(title, onBack, buildContent, buildFooter)
    }

    private fun createPageSurface(
        title: String,
        onBack: () -> Unit,
        buildContent: (LinearLayout) -> Unit,
        buildFooter: ((LinearLayout) -> Unit)?
    ): View {
        val page = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            isClickable = true
            isFocusable = true
            setBackgroundColor(
                ContextCompat.getColor(activity, R.color.browser_background)
            )
        }
        page.addView(
            surfaceFactory.createToolbar(title, onBack),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
            )
        )

        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(24))
        }
        buildContent(content)

        page.addView(
            scrollable(content),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        buildFooter?.let { footerBuilder ->
            val footer = LinearLayout(activity).apply {
                orientation = LinearLayout.VERTICAL
            }
            footerBuilder(footer)
            if (footer.childCount > 0) {
                page.addView(
                    footer,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }
        return page
    }

    fun createBottomSheetPage(
        title: String,
        onBack: (() -> Unit)?,
        onClose: () -> Unit,
        buildContent: (LinearLayout) -> Unit
    ): View {
        val overlay = FrameLayout(activity).apply {
            isClickable = true
            isFocusable = true
            setBackgroundColor(ContextCompat.getColor(activity, R.color.browser_sheet_scrim))
            setOnClickListener { onClose() }
        }

        val sheet = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            isClickable = true
            isFocusable = true
            elevation = dp(12).toFloat()
            background = surfaceFactory.createBottomSheetBackground(
                ContextCompat.getColor(activity, R.color.browser_surface)
            )
        }

        sheet.addView(
            surfaceFactory.createSheetHandle(),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(18)
            )
        )
        sheet.addView(
            surfaceFactory.createSheetToolbar(title, onBack, onClose),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
            )
        )

        val content = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), 0, dp(14), dp(20))
        }
        buildContent(content)

        sheet.addView(
            scrollable(content),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        overlay.addView(
            sheet,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                bottomSheetHeight()
            ).apply {
                gravity = Gravity.BOTTOM
            }
        )
        return overlay
    }

    private fun scrollable(content: LinearLayout): ScrollView {
        return ScrollView(activity).apply {
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun bottomSheetHeight(): Int {
        val displayHeight = activity.resources.displayMetrics.heightPixels
        return (displayHeight * 0.54f)
            .toInt()
            .coerceAtLeast(dp(360))
            .coerceAtMost(displayHeight - dp(24))
    }
}
