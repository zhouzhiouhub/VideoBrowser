package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“页面增强装配模块”。
 * 文件名 BrowserPageFeatureAssemblyController 可以拆开理解为“Browser Page Feature Assembly Controller”，
 * 表示它只负责创建 JS 注入、页面增强协调、元素选择器和网页原生桥控制器。
 * 阅读顺序：先看 BrowserPageFeatureComponents 知道返回哪些对象，再看 create() 中脚本注入、元素选择和 native bridge 如何连接。
 */
import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import com.example.videobrowser.element.ElementPickerController
import com.example.videobrowser.inject.JsInjector
import com.example.videobrowser.inject.PageFeatureCoordinator
import com.example.videobrowser.inject.PageFeatureInjectionController
import com.example.videobrowser.inject.ScriptLoader
import com.example.videobrowser.rules.RuleEngine
import com.example.videobrowser.settings.SettingsManager
import com.example.videobrowser.video.FullscreenVideoController
import com.example.videobrowser.video.WebPlaybackHistoryRecorder

/**
 * 页面增强组件集合。
 *
 * @param jsInjector 参数类型为 `JsInjector`，表示读取并组合 assets/scripts 中脚本后注入当前页面的控制器。
 * @param pageFeatureCoordinator 参数类型为 `PageFeatureCoordinator`，表示按全局设置和站点例外判断页面增强是否启用的协调器。
 * @param elementPickerController 参数类型为 `ElementPickerController`，表示网页元素选择、屏蔽和取消流程控制器。
 * @param nativeBridgeController 参数类型为 `VideoBrowserNativeBridgeController`，表示注入给网页调用的 Android 原生桥控制器。
 */
data class BrowserPageFeatureComponents(
    val jsInjector: JsInjector,
    val pageFeatureCoordinator: PageFeatureCoordinator,
    val elementPickerController: ElementPickerController,
    val nativeBridgeController: VideoBrowserNativeBridgeController
)

/**
 * 页面增强装配控制器。
 *
 * 页面增强链路包括三层：JsInjector 负责实际执行脚本，PageFeatureCoordinator 负责判断启用状态，
 * ElementPickerController 和 VideoBrowserNativeBridgeController 负责页面内交互回传。
 *
 * @param activity 参数类型为 `AppCompatActivity`，表示元素选择器显示提示和读取资源的宿主 Activity。
 * @param assets 参数类型为 `AssetManager`，表示读取 assets/scripts 下内置脚本的资源管理器。
 * @param settingsManager 参数类型为 `SettingsManager`，表示读取页面增强、站点例外和元素屏蔽设置的数据源。
 * @param ruleEngine 参数类型为 `RuleEngine`，表示脚本注入时使用的规则引擎。
 * @param browserManager 参数类型为 `() -> BrowserManager`，表示读取当前 active BrowserManager 的回调。
 * @param browserSessionStateController 参数类型为 `BrowserSessionStateController`，表示读取当前页面 URL 的会话状态控制器。
 * @param browserUrlStateController 参数类型为 `BrowserUrlStateController`，表示读取当前站点 host 的控制器。
 * @param browserFeatureStateController 参数类型为 `BrowserFeatureStateController`，表示读取 JS 注入和站点禁用状态的控制器。
 * @param pageFeatureInjectionController 参数类型为 `PageFeatureInjectionController`，表示在元素选择后重新注入页面增强的控制器。
 * @param browserChromeClientStateController 参数类型为 `BrowserChromeClientStateController`，表示读取当前 ChromeClient 的状态控制器。
 * @param fullscreenVideoController 参数类型为 `FullscreenVideoController`，表示 native bridge 更新视频全屏时间线时使用的控制器。
 * @param webPlaybackHistoryRecorder 参数类型为 `WebPlaybackHistoryRecorder`，表示 native bridge 记录网页播放历史的数据写入控制器。
 * @param postToUi 参数类型为 `(() -> Unit) -> Unit`，表示把网页线程回调切回 Android UI 线程执行的函数。
 */
class BrowserPageFeatureAssemblyController(
    private val activity: AppCompatActivity,
    private val assets: AssetManager,
    private val settingsManager: SettingsManager,
    private val ruleEngine: RuleEngine,
    private val browserManager: () -> BrowserManager,
    private val browserSessionStateController: BrowserSessionStateController,
    private val browserUrlStateController: BrowserUrlStateController,
    private val browserFeatureStateController: BrowserFeatureStateController,
    private val pageFeatureInjectionController: PageFeatureInjectionController,
    private val browserChromeClientStateController: BrowserChromeClientStateController,
    private val fullscreenVideoController: FullscreenVideoController,
    private val webPlaybackHistoryRecorder: WebPlaybackHistoryRecorder,
    private val postToUi: (() -> Unit) -> Unit
) {
    /**
     * 创建页面增强组件集合。
     *
     * @return 返回 `BrowserPageFeatureComponents`，调用方把其中对象保存到 MainActivity 字段后供会话、生命周期和启动流程使用。
     */
    fun create(): BrowserPageFeatureComponents {
        val jsInjector = JsInjector(
            scriptLoader = ScriptLoader(assets),
            evaluateJavascript = { script ->
                browserManager().evaluateJavascript(script)
            },
            ruleEngine = ruleEngine
        )
        val pageFeatureCoordinator = PageFeatureCoordinator(
            settingsManager = settingsManager,
            browserManager = browserManager,
            jsInjector = jsInjector,
            currentSiteHost = browserUrlStateController::currentSiteHost,
            currentPageUrl = {
                browserSessionStateController.currentSessionController().currentPageUrl
            }
        )
        val elementPickerController = ElementPickerController(
            activity = activity,
            browserManager = browserManager,
            settingsManager = settingsManager,
            currentSiteHost = browserUrlStateController::currentSiteHost,
            isJsInjectionEnabled = browserFeatureStateController::isJsInjectionEnabled,
            isCurrentSiteJsInjectionDisabled =
                browserFeatureStateController::isCurrentSiteJsInjectionDisabled,
            injectPageFeatures = pageFeatureInjectionController::injectPageFeatures
        )
        val nativeBridgeController = VideoBrowserNativeBridgeController(
            postToUi = postToUi,
            currentChromeClient = browserChromeClientStateController::currentChromeClientOrNull,
            fullscreenVideoController = fullscreenVideoController,
            webPlaybackHistoryRecorder = webPlaybackHistoryRecorder,
            requestElementBlock = elementPickerController::handlePickedElement,
            blockSelectedElement = { selector ->
                elementPickerController.handlePickedElement(selector, "")
            },
            cancelElementPicker = elementPickerController::handleCancelledFromPage
        )
        return BrowserPageFeatureComponents(
            jsInjector = jsInjector,
            pageFeatureCoordinator = pageFeatureCoordinator,
            elementPickerController = elementPickerController,
            nativeBridgeController = nativeBridgeController
        )
    }
}
