package com.example.videobrowser.functioncenter

/**
 * 初学者阅读提示：
 * 这个文件属于“功能中心模块”。
 * 文件名 FunctionCenterEntryController 可以拆开理解为“Function Center Entry Controller”，
 * 表示它只负责 MainActivity 进入或关闭功能中心的入口动作。
 * 主要职责：打开功能中心根页、个人页、当前站点设置页，以及处理功能中心返回和关闭。
 * 阅读顺序：先看构造参数了解它依赖谁，再看公开函数对应 MainActivity 里的哪些入口。
 */

/**
 * 功能中心入口控制器。
 *
 * FunctionCenterPages 负责具体页面编排；这个控制器负责进入这些页面前后需要做的公共动作，
 * 例如打开页面前先隐藏软键盘。
 *
 * @param functionCenterPages 功能中心页面编排器，负责显示根页、个人页、站点设置页和处理返回。
 * @param hideKeyboard 隐藏软键盘的函数，打开用户主动进入的功能中心页面前会调用。
 */
class FunctionCenterEntryController(
    private val functionCenterPages: FunctionCenterPages,
    private val hideKeyboard: () -> Unit
) {
    /**
     * 打开功能中心根页，并先隐藏软键盘。
     *
     * @return 无返回值；页面显示由 FunctionCenterPages 负责。
     */
    fun showFunctionCenter() {
        hideKeyboard()
        functionCenterPages.showRootPage()
    }

    /**
     * 直接打开功能中心根页。
     *
     * @return 无返回值；用于已经处于功能中心流程内的回跳，不额外处理键盘。
     */
    fun showFunctionCenterRootPage() {
        functionCenterPages.showRootPage()
    }

    /**
     * 打开个人中心页，并先隐藏软键盘。
     *
     * @return 无返回值；页面显示由 FunctionCenterPages 负责。
     */
    fun showProfilePage() {
        hideKeyboard()
        functionCenterPages.showProfilePage()
    }

    /**
     * 打开当前站点设置页，并先隐藏软键盘。
     *
     * @return 无返回值；页面显示由 FunctionCenterPages 负责。
     */
    fun showCurrentSiteSettingsPage() {
        hideKeyboard()
        functionCenterPages.showCurrentSiteSettingsPage()
    }

    /**
     * 让功能中心处理一次返回动作。
     *
     * @return true 表示功能中心消费了返回动作，false 表示调用方应继续处理浏览器返回。
     */
    fun handleFunctionCenterBack(): Boolean {
        return functionCenterPages.handleBack()
    }

    /**
     * 关闭功能中心。
     *
     * @return true 表示功能中心原本处于显示状态并已关闭，false 表示无需关闭。
     */
    fun closeFunctionCenter(): Boolean {
        return functionCenterPages.close()
    }
}
