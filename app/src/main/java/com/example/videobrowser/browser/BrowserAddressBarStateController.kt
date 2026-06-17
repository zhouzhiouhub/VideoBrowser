package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器地址栏状态模块”。
 * 地址栏既要显示当前 URL，也要把搜索结果 URL 还原成搜索词，并同步站点安全图标。
 * 阅读顺序：先看 updateAddressBar，再看 addressBarDisplayText 和 isProviderHomeUrl。
 */
import android.widget.EditText
import com.example.videobrowser.browser.search.SearchProviderController

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
    /**
     * 刷新地址栏文本和站点安全图标状态。
     *
     * @param url 当前页面 URL；为空时只同步站点安全状态，不改地址栏文本。
     */
    fun updateAddressBar(url: String?) {
        siteSecurityController()?.updateStatus(url)
        if (url.isNullOrBlank()) {
            return
        }

        val displayUrl = addressBarDisplayText(url)
        if (addressInput.text?.toString() == displayUrl) {
            return
        }
        addressInput.setText(displayUrl)
        addressInput.setSelection(addressInput.text?.length ?: 0)
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

    /**
     * 判断 URL 是否为任一搜索提供方首页。
     *
     * @param url 待判断的 URL，可为空。
     * @return true 表示该地址是搜索提供方首页，浏览器应按首页状态展示。
     */
    fun isProviderHomeUrl(url: String?): Boolean {
        return searchProviderController.isProviderHomeUrl(url)
    }
}
