package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 PrivateBrowsingSwitchController 可以拆开理解为“Private Browsing Switch Controller”，
 * 表示它只负责普通浏览和无痕浏览之间的切换。
 * 主要职责：关闭临时界面、清理本次会话权限、切换 WebView 会话，并刷新浏览器 UI。
 * 阅读顺序：先看构造参数了解它会调用哪些外部动作，再看 setPrivateBrowsingActive() 的分支顺序。
 */
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.R
import com.example.videobrowser.settings.SessionSitePermissionStore
import com.example.videobrowser.utils.ShortToast

/**
 * 无痕浏览切换控制器。
 *
 * 这个类不直接保存无痕状态；真正状态仍由 BrowserSessionCoordinator 切换 WebView 后回调给 MainActivity。
 *
 * @param activity 当前 Activity，用于在无痕 WebView 创建失败时显示失败提示。
 * @param isPrivateBrowsingActive 返回当前是否处于无痕模式的函数，用于避免重复切换。
 * @param closeFunctionCenter 关闭功能中心面板的函数，切换模式前会先调用。
 * @param cancelElementPickerIfActive 取消元素选择器的函数；如果当前没有选择流程，调用方应自行空操作。
 * @param exitPageFullscreenIfNeeded 退出网页全屏状态的函数，避免切换 WebView 时残留全屏 UI。
 * @param sessionSitePermissionStore 本次会话权限存储；切换模式时清空临时站点权限。
 * @param browserSessionCoordinator 浏览器会话协调器，负责真正进入或退出无痕 WebView。
 * @param privateSessionController 无痕会话状态控制器，进入无痕后会重置为空白主页状态。
 * @param standardSessionController 普通会话状态控制器，退出无痕后会把普通页面状态重新渲染到 UI。
 * @param openHomePage 打开主页的函数，进入无痕成功后用于显示无痕首页。
 * @param updatePrivateBrowsingUi 刷新无痕模式主题、标识和相关颜色的函数。
 * @param updateNavigationButtons 刷新底部导航按钮状态的函数。
 */
class PrivateBrowsingSwitchController(
    private val activity: AppCompatActivity,
    private val isPrivateBrowsingActive: () -> Boolean,
    private val closeFunctionCenter: () -> Unit,
    private val cancelElementPickerIfActive: () -> Unit,
    private val exitPageFullscreenIfNeeded: () -> Unit,
    private val sessionSitePermissionStore: SessionSitePermissionStore,
    private val browserSessionCoordinator: BrowserSessionCoordinator,
    private val privateSessionController: BrowserSessionController,
    private val standardSessionController: BrowserSessionController,
    private val openHomePage: () -> Unit,
    private val updatePrivateBrowsingUi: () -> Unit,
    private val updateNavigationButtons: () -> Unit
) {
    /**
     * 切换当前无痕浏览状态。
     *
     * @param enabled true 表示进入无痕模式，false 表示回到普通浏览模式。
     * @return 无返回值；切换失败时会显示提示并保留原状态。
     */
    fun setPrivateBrowsingActive(enabled: Boolean) {
        if (enabled == isPrivateBrowsingActive()) {
            updatePrivateBrowsingUi()
            return
        }

        closeFunctionCenter()
        cancelElementPickerIfActive()
        exitPageFullscreenIfNeeded()
        sessionSitePermissionStore.clear()

        if (enabled) {
            val started = browserSessionCoordinator.enterPrivate()
            if (!started) {
                ShortToast.show(activity, R.string.toast_private_browsing_failed)
                return
            }
            privateSessionController.reset()
            openHomePage()
        } else {
            browserSessionCoordinator.exitPrivate()
            standardSessionController.renderCurrentState(forceProgressHidden = true)
        }
        updatePrivateBrowsingUi()
        updateNavigationButtons()
    }
}
