package com.example.videobrowser.video

/**
 * 初学者阅读提示：
 * 这个文件属于“视频播放模块”。
 * 文件名 VideoSpeedOptions 可以拆开理解为“Video Speed Options”，表示它只负责视频链路中的一个小职责。
 * 主要职责：连接网页视频手势、原生 ExoPlayer 播放、播放队列、字幕、播放历史或媒体路由。
 * 阅读顺序：先看数据模型表达什么播放状态，再看控制器如何响应用户手势和播放器回调。
 */
object VideoSpeedOptions {
    val longPressSpeed = 2f

    private val menuSpeeds = listOf(
        0.5f,
        0.75f,
        1f,
        1.25f,
        1.5f,
        2f,
        2.5f,
        3f
    )

    /**
     * 函数 `menuSpeeds`：封装 `menu Speeds` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun menuSpeeds(): List<Float> = menuSpeeds
}
