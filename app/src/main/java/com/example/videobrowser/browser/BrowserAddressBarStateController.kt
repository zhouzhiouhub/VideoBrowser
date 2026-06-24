package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器地址栏状态模块”。
 * 地址栏既要显示当前 URL，也要把搜索结果 URL 还原成搜索词，并同步站点安全图标。
 * 阅读顺序：先看 updateAddressBar，再看 addressBarDisplayText。
 */
import android.widget.EditText
import com.example.videobrowser.browser.search.SearchProviderController
import com.example.videobrowser.utils.UrlUtils

/**
 * 地址栏状态控制器。
 *
 * MainActivity 只负责把地址输入框和搜索提供方控制器传进来；具体显示文本和站点安全刷新逻辑集中在这里。
 *
 * @param addressInput 地址栏输入框，本控制器会更新它的文本和光标位置。
 * @param searchProviderController 搜索提供方控制器，用来判断搜索首页和把搜索 URL 转回展示文本。
 * @param siteSecurityController 读取站点安全控制器的函数；未初始化时返回 null，避免早期页面状态刷新崩溃。
 */
class BrowserAddressBarStateController(
    private val addressInput: EditText,
    private val searchProviderController: SearchProviderController,
    private val siteSecurityController: () -> SiteSecurityController?
) {
    private var currentUrl: String? = null
    private var isAddressInputFocused = false

    /**
     * 刷新地址栏文本和站点安全图标状态。
     *
     * @param url 当前页面 URL；为空时清空地址栏，让首页状态只显示输入提示。
     */
    fun updateAddressBar(url: String?) {
        siteSecurityController()?.updateStatus(url)
        currentUrl = url?.takeUnless { value -> value.isBlank() }
        if (url.isNullOrBlank()) {
            if (!addressInput.text.isNullOrEmpty()) {
                addressInput.setText("")
            }
            return
        }

        updateAddressInput(
            text = addressTextFor(url, isAddressInputFocused),
            selectAll = isAddressInputFocused
        )
    }

    /**
     * 地址栏聚焦时临时显示完整链接，便于复制；失焦后恢复搜索结果关键词展示。
     *
     * @param hasFocus true 表示用户正在直接操作地址栏。
     */
    fun handleAddressFocusChanged(hasFocus: Boolean) {
        isAddressInputFocused = hasFocus
        val url = currentUrl ?: return
        updateAddressInput(
            text = addressTextFor(url, hasFocus),
            selectAll = hasFocus
        )
    }

    /**
     * 计算地址栏应该展示的文本。
     *
     * @param url 当前页面 URL，可能是搜索结果页、搜索首页或普通网页。
     * @return 搜索首页返回空字符串，搜索结果页返回搜索词，普通网页返回可读 URL。
     */
    fun addressBarDisplayText(url: String): String {
        return searchProviderController.addressBarDisplayText(url)
    }

    private fun addressTextFor(url: String, hasFocus: Boolean): String {
        return if (hasFocus) {
            UrlUtils.displayUrl(url)
        } else {
            addressBarDisplayText(url)
        }
    }

    private fun updateAddressInput(text: String, selectAll: Boolean) {
        if (addressInput.text?.toString() != text) {
            addressInput.setText(text)
        }
        if (selectAll) {
            addressInput.selectAll()
        } else {
            addressInput.setSelection(addressInput.text?.length ?: 0)
        }
    }

}
