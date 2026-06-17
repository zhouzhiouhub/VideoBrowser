package com.example.videobrowser.inject

/**
 * 初学者阅读提示：
 * 这个文件属于“页面增强注入入口模块”。
 * PageFeatureCoordinator 负责真正判断和注入脚本；本类只负责给较早创建的控制器提供一个安全入口，
 * 避免 MainActivity 到处写 pageFeatureCoordinator 是否已经初始化的判断。
 */

/**
 * 页面增强注入入口控制器。
 *
 * @param pageFeatureCoordinator 参数类型为 `() -> PageFeatureCoordinator?`，表示返回页面增强协调器的函数；尚未初始化时返回 null。
 */
class PageFeatureInjectionController(
    private val pageFeatureCoordinator: () -> PageFeatureCoordinator?
) {
    /**
     * 安全触发一次页面增强注入。
     *
     * @return 无返回值；页面增强协调器尚未初始化时直接跳过。
     */
    fun injectPageFeatures() {
        pageFeatureCoordinator()?.injectPageFeatures()
    }
}
