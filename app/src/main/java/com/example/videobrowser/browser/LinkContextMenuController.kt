package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“网页长按菜单模块”。
 * 文件名 LinkContextMenuController 可以拆开理解为“Link Context Menu Controller”，表示它专门负责 WebView 长按链接或图片时的操作菜单。
 * 主要职责：识别 WebView 命中的链接/图片 URL，展示打开、下载、复制和分享菜单，并把具体动作分发给浏览器或系统能力。
 * 阅读顺序：先看 configure，再看 linkHitTestUrl/imageHitTestUrl，最后看 showLinkContextMenu/showImageContextMenu。
 */
import android.content.Intent
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.utils.PageUrlActions

/**
 * WebView 链接和图片长按菜单控制器。
 *
 * MainActivity 只负责在 WebView 创建或切换时调用 configure；本类负责长按命中判断和菜单动作。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示当前屏幕宿主，用来创建菜单、访问剪贴板、启动系统分享面板和读取字符串资源。
 * @param openUrlInNewTab 参数类型为 `(String) -> Unit`，表示在新标签页打开 URL 的回调，用户选择“新标签打开”时调用。
 * @param downloadUrl 参数类型为 `(String, String?) -> Unit`，表示下载 URL 的回调，第一个参数是下载地址，第二个参数是当前 User-Agent，可能为空。
 * @param currentUserAgent 参数类型为 `() -> String?`，表示读取当前 WebView User-Agent 的回调，下载链接或图片时传给下载模块。
 * @param isShareableUrl 参数类型为 `(String) -> Boolean`，表示判断 URL 是否可用于菜单动作的回调，只允许 http/https 这类可分享地址进入菜单。
 */
class LinkContextMenuController(
    private val activity: AppCompatActivity,
    private val openUrlInNewTab: (String) -> Unit,
    private val downloadUrl: (String, String?) -> Unit,
    private val currentUserAgent: () -> String?,
    private val isShareableUrl: (String) -> Boolean
) {
    /**
     * 函数 `configure`：给目标 WebView 安装长按链接/图片菜单监听。
     *
     * 初学者阅读提示：WebView 会通过 hitTestResult 告诉我们当前长按命中了链接、图片还是普通页面内容。
     *
     * @param targetWebView 参数类型为 `WebView`，表示需要绑定长按菜单的 WebView，标准/无痕/新标签页 WebView 都会调用。
     */
    fun configure(targetWebView: WebView) {
        targetWebView.setOnLongClickListener { view ->
            val hitTestResult = (view as? WebView)?.hitTestResult
                ?: return@setOnLongClickListener false
            linkHitTestUrl(hitTestResult)?.let { linkUrl ->
                showLinkContextMenu(linkUrl)
                return@setOnLongClickListener true
            }
            val imageUrl = imageHitTestUrl(hitTestResult)
                ?: return@setOnLongClickListener false
            showImageContextMenu(imageUrl)
            true
        }
    }

    /**
     * 函数 `imageHitTestUrl`：从 WebView 命中结果中提取图片 URL。
     *
     * 初学者阅读提示：只有 IMAGE_TYPE 才代表独立图片命中，提取后还会过滤掉非 http/https 地址。
     *
     * @param hitTestResult 参数类型为 `WebView.HitTestResult?`，表示 WebView 长按命中的页面元素信息，可能为空。
     * @return 返回可操作的图片 URL；如果没有命中图片或 URL 不可分享，则返回 null。
     */
    private fun imageHitTestUrl(hitTestResult: WebView.HitTestResult?): String? {
        if (hitTestResult?.type != WebView.HitTestResult.IMAGE_TYPE) {
            return null
        }
        return hitTestResult.extra
            ?.trim()
            ?.takeIf(isShareableUrl)
    }

    /**
     * 函数 `showImageContextMenu`：展示图片长按菜单。
     *
     * 初学者阅读提示：图片菜单复用链接下载/复制逻辑，但分享标题使用图片专属文案。
     *
     * @param url 参数类型为 `String`，表示用户长按命中的图片地址，会传给打开、下载、复制或分享动作。
     */
    private fun showImageContextMenu(url: String) {
        val actions = arrayOf(
            activity.getString(R.string.action_open_image_new_tab),
            activity.getString(R.string.action_download_image),
            activity.getString(R.string.action_copy_image_link),
            activity.getString(R.string.action_share_image_link)
        )
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_image_context_menu)
            .setItems(actions) { dialog, which ->
                when (which) {
                    0 -> openUrlInNewTab(url)
                    1 -> downloadImageUrl(url)
                    2 -> copyImageUrl(url)
                    3 -> shareImageUrl(url)
                }
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 函数 `downloadImageUrl`：下载图片 URL。
     *
     * 初学者阅读提示：图片下载和链接下载参数一致，所以这里直接复用 downloadLinkUrl。
     *
     * @param url 参数类型为 `String`，表示要下载的图片地址。
     */
    private fun downloadImageUrl(url: String) {
        downloadLinkUrl(url)
    }

    /**
     * 函数 `copyImageUrl`：复制图片 URL 到剪贴板。
     *
     * 初学者阅读提示：图片 URL 和普通链接 URL 使用相同剪贴板标签，复制后都会提示用户已复制。
     *
     * @param url 参数类型为 `String`，表示要复制的图片地址。
     */
    private fun copyImageUrl(url: String) {
        copyLinkUrl(url)
    }

    /**
     * 函数 `shareImageUrl`：通过系统分享面板分享图片 URL。
     *
     * 初学者阅读提示：这里只分享图片地址文本，不直接下载或转发图片二进制内容。
     *
     * @param url 参数类型为 `String`，表示要分享的图片地址。
     */
    private fun shareImageUrl(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        activity.startActivity(
            Intent.createChooser(intent, activity.getString(R.string.action_share_image_link))
        )
    }

    /**
     * 函数 `linkHitTestUrl`：从 WebView 命中结果中提取链接 URL。
     *
     * 初学者阅读提示：普通链接和“图片外层包链接”的命中类型都算链接，提取后同样过滤非 http/https 地址。
     *
     * @param hitTestResult 参数类型为 `WebView.HitTestResult?`，表示 WebView 长按命中的页面元素信息，可能为空。
     * @return 返回可操作的链接 URL；如果没有命中链接或 URL 不可分享，则返回 null。
     */
    private fun linkHitTestUrl(hitTestResult: WebView.HitTestResult?): String? {
        val type = hitTestResult?.type ?: return null
        if (type != WebView.HitTestResult.SRC_ANCHOR_TYPE &&
            type != WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
        ) {
            return null
        }
        return hitTestResult.extra
            ?.trim()
            ?.takeIf(isShareableUrl)
    }

    /**
     * 函数 `showLinkContextMenu`：展示链接长按菜单。
     *
     * 初学者阅读提示：链接菜单提供新标签打开、下载、复制和分享，不提供外部浏览器打开，避免跳出当前浏览器体验。
     *
     * @param url 参数类型为 `String`，表示用户长按命中的链接地址，会传给打开、下载、复制或分享动作。
     */
    private fun showLinkContextMenu(url: String) {
        val actions = arrayOf(
            activity.getString(R.string.action_open_link_new_tab),
            activity.getString(R.string.action_download_link),
            activity.getString(R.string.action_copy_link),
            activity.getString(R.string.action_share_link)
        )
        AlertDialog.Builder(activity)
            .setTitle(R.string.title_link_context_menu)
            .setItems(actions) { dialog, which ->
                when (which) {
                    0 -> openUrlInNewTab(url)
                    1 -> downloadLinkUrl(url)
                    2 -> copyLinkUrl(url)
                    3 -> shareLinkUrl(url)
                }
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 函数 `downloadLinkUrl`：把链接 URL 交给下载模块。
     *
     * 初学者阅读提示：下载模块需要 URL 和当前 User-Agent；contentDisposition 和 mimeType 由下载模块后续探测或按默认值处理。
     *
     * @param url 参数类型为 `String`，表示要下载的链接地址。
     */
    private fun downloadLinkUrl(url: String) {
        downloadUrl(url, currentUserAgent())
    }

    /**
     * 函数 `copyLinkUrl`：复制链接 URL 到剪贴板。
     *
     * 初学者阅读提示：复制链接统一交给共享页面 URL 工具，保持剪贴板标签和提示文案一致。
     *
     * @param url 参数类型为 `String`，表示要复制的链接地址。
     */
    private fun copyLinkUrl(url: String) {
        PageUrlActions.copyPageUrl(activity, url)
    }

    /**
     * 函数 `shareLinkUrl`：通过系统分享面板分享链接 URL。
     *
     * 初学者阅读提示：这里使用 ACTION_SEND 分享纯文本，让系统选择微信、浏览器、笔记等可处理文本的应用。
     *
     * @param url 参数类型为 `String`，表示要分享的链接地址。
     */
    private fun shareLinkUrl(url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        activity.startActivity(
            Intent.createChooser(intent, activity.getString(R.string.action_share_link))
        )
    }
}
