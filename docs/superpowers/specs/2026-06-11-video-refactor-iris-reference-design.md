# 参考 Iris 重构视频播放逻辑设计

## 目的

本文回答一个具体问题：重构 VideoBrowser 当前的视频播放相关逻辑时，是否可以参考 `refence/iris` 项目，并据此完善浏览器中的视频操作。

结论：可以参考，但不应照搬。`iris` 对 VideoBrowser 的价值主要在“产品能力清单”和“模块边界设计”，不是 Flutter、MediaKit、FVP 这些具体技术栈。VideoBrowser 应继续保留 Android WebView + AndroidX Media3 的架构，借鉴 `iris` 的播放状态、队列、历史续播、字幕音轨、手势语义和控制模型。

## 当前 VideoBrowser 视频现状

VideoBrowser 现在有两条视频路径。

1. 网页内视频：网页 `<video>` 由 WebView、注入脚本、站点适配器、`VideoBrowserNativeBridge`、`ChromeClient` 和 `FullscreenVideoController` 协同控制。
2. 直链和本地文件：可播放 URL、下载监听中的媒体 URL、本地 SAF 文档会打开 `PlayerActivity`，由 Media3 `ExoPlayer` 播放。

已有优势：

- `MediaUrlUtils` 已按 scheme、扩展名、MIME、流媒体 manifest、RTSP 识别可播放媒体。
- `DownloadController`、`MainActivity.loadUrl()`、`MainActivity.shouldBlockUrl()`、`PageActionsController.openLocalDocumentUri()` 已能把直链媒体路由到原生播放器。
- `PlayerActivity` 已支持 Media3 播放、User-Agent、Cookie、Referer、HLS/DASH/SmoothStreaming MIME 归一化、沉浸全屏、倍速、横竖屏、Activity 重建后的播放位置恢复，以及视频增强效果失败后的无效果重试。
- `FullscreenVideoGestureOverlay` 已覆盖点击播放暂停、双击快进快退、横滑 seek 预览、亮度、音量、倍速菜单、锁定、旋转、侧边长按扫描和退出按钮能力。
- WebView 路径已有适合浏览器的抽象：通用脚本会先调用站点 `videoCapabilities`，再回退到通用 HTMLVideoElement 操作。
- 现有测试已保护 JS 注入、站点能力代理、全屏退出、诊断日志、默认倍速、WebView 视频状态等契约。

当前缺口：

- 原生播放器只保存 Activity 生命周期内状态和全局默认倍速，没有像 `iris` 那样的按媒体持久化观看历史。
- 直链和本地视频没有一等播放队列，也没有 repeat/shuffle 模型。
- 本地文件浏览打开的是单个文档，没有像 `iris` 一样从目录构建播放列表。
- 字幕和音轨选择没有作为原生播放器的一等操作暴露出来。
- WebView 视频和原生播放器共用了手势 UI，但没有一份清晰的 Kotlin 侧视频动作、状态和能力回退模型。
- `PlayerActivity` 的覆盖层有退出回调属性，但原生播放器路径尚未把它绑定到 `finish()`；WebView 全屏路径已经绑定。
- `MediaUrlUtils`、本地媒体分发、原生播放器行为级测试覆盖弱于 WebView 注入路径。

## Iris 可参考内容

`refence/iris` 是 Flutter 跨平台播放器。它最值得参考的点是：

- 统一 `MediaPlayer` 接口：播放/暂停、seek、快进快退、逐帧、位置、时长、缓冲、尺寸、保存进度、字幕、音轨。
- `FileItem` 模型：存储类型、URI、路径、内容类型、大小、修改时间和字幕列表都在同一个媒体模型中。
- 播放队列状态：当前索引、上一项/下一项、添加/移除、随机、排序、单个循环/全部循环/无循环。
- 持久化历史和续播：按文件身份保存播放进度，并在下次打开时恢复。
- 本地/WebDAV/FTP 文件浏览和同名字幕自动关联。
- 统一控制和手势：倍速、音量、亮度、全屏、画面缩放、字幕音轨、队列、存储、历史。

真正应该借鉴的是这些职责边界：媒体来源模型、队列模型、播放器控制模型、UI 状态、持久化历史。

## 应借鉴的能力

- 建一个小的原生播放领域模型，类似 `iris` 的 `MediaPlayer`，但用 Kotlin + Media3 实现。
- 增加 `PlayableMediaItem`：统一 URL、content URI、标题、MIME、请求头、Referer、User-Agent、来源类型和可选字幕候选。
- 增加 `PlaybackSessionState`：位置、时长、倍速、repeat 模式、当前 item index、playWhenReady。
- 增加持久化续播：参考 `iris` 的近结尾阈值，剩余小于等于 5 秒时不再自动续播。
- 本地 SAF 目录中打开视频时，可以可选地构建同目录 sibling 播放队列。
- 增加队列操作：上一项、下一项、添加、移除、repeat none/one/all。shuffle 可以后置。
- 先支持本地字幕检测和关联，远程字幕等有明确来源后再做。
- 基于 Media3 track 能力暴露字幕/音轨选择。
- 保留当前手势层，实现更清晰的反馈和统一动作语义。
- 补强媒体识别、路由、状态持久化、队列边界、播放器动作的测试矩阵。

## 不应照搬的内容

- 不迁移 Flutter UI、hooks、Zustand 风格 store、MediaKit 或 FVP。VideoBrowser 是 Android 原生项目，已有 Media3。
- 不照搬桌面窗口管理、Windows 文件关联、拖拽、键盘快捷键。
- WebDAV/FTP 不作为第一阶段目标。它们是播放器存储能力，不是浏览器视频重构的核心。
- 不引入 `MANAGE_EXTERNAL_STORAGE` 这类宽权限。VideoBrowser 当前使用 SAF 是更合适的 Android 方案。
- 不把 `iris` 的 0.25x 到 10x 倍速范围作为浏览器默认体验。当前 0.75x 到 3x 对 WebView 站点播放器更稳。
- 不采用横滑时持续 seek 的实现。当前原生覆盖层“预览后松手提交”的策略更稳。
- 不增加第二条进度条。现有测试明确要求 `FullscreenVideoGestureOverlay` 不渲染第二条 progress bar。

## 推荐方案

采用渐进式重构，不做播放器重写。

### 小节 1：定义原生播放领域边界

新增一个小的领域层，放在 `video/` 或 `media/` 下。

- `PlayableMediaItem`：标题、URI、MIME、来源类型、headers、Referer、User-Agent、本地身份、字幕候选。
- `PlaybackQueueState`：items、currentIndex、repeatMode、可选 shuffle、上一项/下一项/播放完成行为。
- `PlaybackProgress`：media identity、position、duration、speed、updatedAt。
- `PlaybackCommand`：play、pause、seekBy、seekTo、setSpeed、next、previous、toggleRepeat、selectTrack、selectSubtitle。

第一步只包装当前 `PlayerActivity` intent extras，不改变用户行为。目标是减少 `MainActivity`、`DownloadController`、`PageActionsController`、`BrowserExternalNavigator` 里的分散分支。

### 小节 2：集中媒体路由

新增一个媒体路由服务。

输入来源：

- 地址栏 URL。
- WebView URL override。
- 下载监听事件。
- 本地 SAF 文档。
- 功能中心操作。

输出结果：

- 打开原生播放器。
- 继续用 WebView 加载。
- 交给外部应用。
- 进入 DownloadManager 下载。

这一阶段要保持行为不变。价值在于把分散的 `MediaUrlUtils.isPlayableMediaUri()` 判断集中起来。

### 小节 3：增加原生播放历史和续播

新增基于 `PreferenceStore` 的播放历史仓库。

- 远程媒体用 URL 做 identity。
- SAF 文档用 `content://` URI 做 identity。
- 在 pause、stop、Activity finish 前保存 position 和 duration。
- duration 已知且剩余时间大于 5 秒时才续播。
- 历史数量做上限，例如 100 条。
- 从网页来源打开的媒体在无痕模式下不写历史。

这是最接近 `iris`、且用户收益最大的第一批功能。

### 小节 4：引入原生播放队列

在历史稳定后再做队列。

- 单个 URL 创建单项队列。
- 从本地文件浏览器打开时，可从同一 SAF 目录构建 sibling 可播放文件队列。
- `PlayerActivity` 增加 next/previous。
- 增加 repeat none/one/all。
- shuffle 后置，除非 UI 需求明确。

这能复用 `iris` 的队列体验，但保持 Android SAF 友好。

### 小节 5：字幕和音轨选择

使用 Media3 能力，不照搬 `iris` API。

- 从 `ExoPlayer.currentTracks` 暴露可用音轨和文本轨。
- 在原生播放器控制层增加简单轨道选择面板。
- 对本地 SAF 目录，在有目录上下文时检测同名字幕文件。
- 用 Media3 `MediaItem.SubtitleConfiguration` 挂载外部字幕。

字幕发现依赖目录上下文，所以应排在队列之后。

### 小节 6：保留并强化 WebView 视频协议

继续保留现有 JS 能力 broker。

- `VideoBrowserEnhancer.seekBy()`
- `VideoBrowserEnhancer.seekTo()`
- `VideoBrowserEnhancer.togglePlayPause()`
- `VideoBrowserEnhancer.setPlaybackSpeed()`
- `VideoBrowserEnhancer.startDirectionalPlayback()`
- `VideoBrowserEnhancer.stopDirectionalPlayback()`
- `VideoBrowserEnhancer.reportPlaybackTimeline()`

这一层只建议做文档化和 Kotlin 侧类型化，不强行并入原生队列/历史模型。WebView 视频的本质是控制网页播放器，原生视频的本质是 Media3 播放直接媒体，两者边界应保持清楚。

## 目标架构

目标边界如下：

- `utils/MediaUrlUtils`：只做纯媒体识别。
- `video/MediaRoutingController`：决定 URI 应如何处理。
- `video/PlayableMediaItem`：归一化媒体输入。
- `video/PlaybackHistoryRepository`：原生播放续播持久化。
- `video/PlaybackQueue`：原生播放器队列操作。
- `video/PlayerActivity`：Media3 播放宿主和 UI 控制。
- `video/FullscreenVideoGestureOverlay`：只做手势 UI 和反馈。
- `browser/VideoBrowserNativeBridge` 与 `assets/scripts/common.js`：继续作为 WebView 全屏视频控制协议。

核心原则：

- WebView 视频 = 控制网页播放器。
- 原生视频 = 用 Media3 播放直链或本地媒体。

## 分阶段提交计划

小节 1：设计文档。

- 添加本文档。
- 不改应用代码。

小节 2：路由测试和媒体模型。

- 为 `MediaUrlUtils` 补测试。
- 添加 `PlayableMediaItem`。
- 添加 `MediaRoutingController` 决策测试。

小节 3：路由重构。

- 把地址栏、URL override、下载、本地文件打开中的媒体判断收敛到路由层。
- 保持用户行为不变。

小节 4：播放历史。

- 添加 `PlaybackHistoryRepository`。
- `PlayerActivity` 保存并恢复进度。
- 对网页来源媒体遵守无痕模式。

小节 5：播放队列。

- URL 单项队列。
- SAF 文件夹 sibling 队列。
- next/previous 和 repeat none/one/all。

小节 6：字幕和音轨。

- 暴露 Media3 音轨/字幕轨。
- 增加本地字幕候选关联。
- 增加轨道选择 UI 和测试。

每个小节独立提交，避免把设计、重构和功能改动混在一个提交里。当前提交只覆盖小节 1。

## 验证策略

单元测试：

- `MediaUrlUtils`：URL、MIME、HLS/DASH、RTSP、content、file、非媒体。
- `MediaRoutingController`：网页 URL、媒体 URL、SAF 媒体、SAF 非媒体、下载媒体、下载非媒体。
- `PlaybackHistoryRepository`：保存、读取、数量上限、近结尾不续播、无痕不写入。
- 队列：上一项、下一项、repeat 边界。
- 字幕候选：同名匹配、扩展名过滤。

仪器测试：

- 保持现有 WebView 视频回归测试通过。
- 本地 HTML video 全屏仍能上报 timeline，并保持倍速状态。
- 原生播放器能打开测试媒体 URI。
- SAF 路由能区分可播放和不可播放文档。

手工检查：

- 直链 `.mp4`、`.m3u8`、`.mpd`、RTSP。
- 下载监听中媒体进播放器，非媒体进 DownloadManager。
- 本地 SAF 视频可以续播。
- WebView 全屏控制在通用 video 和 Bilibili 上仍可用。
- WebView 全屏和原生播放器的返回/退出行为正确。

## 风险

- WebView 站点播放器脆弱，必须保留当前站点能力代理和通用 fallback。
- DRM、加密流、强绑定站点播放器逻辑，即使 URL 看似可播放，也未必能被原生 Media3 播放。
- SAF 队列和字幕匹配依赖目录授权。
- 远程播放历史可能暴露浏览意图，无痕模式必须跳过写入。
- Media3 track selection 和外部字幕在 HLS/DASH/本地文件上的表现需要分别验证。

## GitHub 提交策略

按小节提交：

1. 小节 1：设计文档。
2. 小节 2：路由测试和媒体模型。
3. 小节 3：无行为变化的路由重构。
4. 小节 4：播放历史。
5. 小节 5：播放队列。
6. 小节 6：字幕和音轨。

每个小节提交前运行对应测试。当前文档是小节 1 的提交范围。
