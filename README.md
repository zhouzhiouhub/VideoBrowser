# VideoBrowser

VideoBrowser 是一个单模块 Android 视频浏览器应用。项目以 Android WebView 为网页容器，围绕视频浏览场景实现了搜索入口、地址建议、广告请求拦截、页面净化、本地 JavaScript 增强、站点适配、网页视频手势、原生播放器、下载记录、本地文件管理和功能中心。

本文严格按当前代码和构建配置整理，不把设计文档中尚未落地的计划项写成已实现能力。

## 下载

[直接下载最新版 APK](https://github.com/zhouzhiouhub/VideoBrowser/releases/latest/download/app-release.apk)

如果 Android 系统提示未知来源安装，需要在系统设置中允许当前浏览器或文件管理器安装应用。

## 项目定位

VideoBrowser 的目标是提供一个偏视频场景的移动浏览器壳层：

- 用 WebView 打开网页，提供前进、后退、刷新、地址栏和页面进度。
- 默认使用百度移动首页，并支持多个搜索入口。
- 对常见广告请求进行本地规则拦截。
- 在页面加载完成后注入应用内置脚本，隐藏广告、弹窗、登录提示等干扰元素，并增强网页视频体验。
- 对直接视频地址或本地视频文件使用 AndroidX Media3 ExoPlayer 原生播放。
- 通过功能中心管理书签、历史、下载、本地文件、站点设置、浏览器设置、拦截日志、白名单、手动屏蔽规则和关于信息。
- 书签和历史记录支持打开、在新标签页打开、复制链接、分享链接和单条移除；收藏夹支持重命名标题。

项目没有后端服务，也没有外部数据库。设置、收藏、历史、下载记录、白名单、自定义快捷入口等数据主要保存在本机 `SharedPreferences` 中。

## 当前功能

### 浏览器基础能力

- 主入口是 `MainActivity`。
- 使用 WebView 加载网页。
- 支持地址栏输入 URL 或搜索关键词。
- 支持前进、后退、刷新、页面进度、页面标题同步。
- 支持系统浏览器打开、复制链接、分享页面、收藏当前页。
- 主页面加载失败时显示本地错误页，并提供原地址重试入口。
- 支持在浏览器设置中修改主页地址。
- 支持在浏览器设置中修改默认搜索引擎。
- 标签页管理支持新建、恢复最近关闭、复制、切换、关闭、关闭其他标签、复制标签链接和分享标签链接。
- 支持桌面模式：切换 User-Agent、开启 wide viewport，并重新加载页面。
- 支持顶部和底部浏览器控件随页面滚动隐藏。
- Debug 包中启用 WebView 调试：`WebView.setWebContentsDebuggingEnabled(true)`。

关键代码：

- `app/src/main/java/com/example/videobrowser/MainActivity.kt`
- `app/src/main/java/com/example/videobrowser/browser/BrowserManager.kt`
- `app/src/main/java/com/example/videobrowser/browser/BrowserClient.kt`
- `app/src/main/java/com/example/videobrowser/browser/ChromeClient.kt`
- `app/src/main/java/com/example/videobrowser/browser/BrowserSessionController.kt`
- `app/src/main/java/com/example/videobrowser/browser/BrowserControlsController.kt`

### 搜索入口和地址建议

默认搜索引擎 ID 是 `baidu`，默认首页是：

```text
https://m.baidu.com/
```

内置搜索入口：

| ID | 名称 | 首页 |
| --- | --- | --- |
| `sogou` | 搜狗 | `https://m.sogou.com/` |
| `so` | 360搜索 | `https://m.so.com/` |
| `quark` | 夸克搜索 | `https://quark.sm.cn/` |
| `uc` | UC | `https://so.m.sm.cn/` |
| `baidu` | 百度 | `https://m.baidu.com/` |
| `edge` | Bing | `https://www.bing.com/` |

地址建议来源：

- 历史记录匹配。
- 远程搜索建议。
- 当前输入关键词的兜底搜索项。

远程建议接口按搜索引擎选择：

- Bing：`https://api.bing.com/osjson.aspx`
- 360：`https://sug.so.360.cn/suggest`
- 其他默认走百度建议接口：`https://suggestion.baidu.com/su`

无痕模式下不会读取历史记录，也不会发起远程搜索建议请求，只保留当前输入的兜底搜索项。

关键代码：

- `app/src/main/java/com/example/videobrowser/browser/search/SearchProvider.kt`
- `app/src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt`
- `app/src/main/java/com/example/videobrowser/browser/search/AddressSuggestionController.kt`
- `app/src/main/java/com/example/videobrowser/browser/search/AddressSuggestionRanker.kt`
- `app/src/main/java/com/example/videobrowser/browser/search/SearchSuggestionClient.kt`

### 广告请求拦截

请求拦截发生在 WebView 的 `shouldInterceptRequest` 回调中。

决策顺序按代码实现为：

1. 全局广告拦截关闭时放行。
2. 主文档请求放行。
3. 非 `http` / `https` 请求放行。
4. 请求 host 命中用户白名单时放行。
5. 当前站点关闭广告拦截时放行。
6. 进入 `RuleEngine` 匹配显式放行规则。
7. 进入 `RuleEngine` 匹配阻断规则。
8. 无命中则放行。

命中阻断时，`AdBlockRequestInterceptor` 默认返回 204 空响应。如果阻断规则带有允许的 `$redirect=` 内置资源，并且请求不是主文档，则返回项目内置 noop 响应。目前只允许 `noopjs`、`noopcss`、`nooptext`，不会加载远程替代资源。

关键代码：

- `app/src/main/java/com/example/videobrowser/adblock/AdBlockRequestPolicy.kt`
- `app/src/main/java/com/example/videobrowser/adblock/AdBlockManager.kt`
- `app/src/main/java/com/example/videobrowser/adblock/AdBlockRequestInterceptor.kt`
- `app/src/main/java/com/example/videobrowser/adblock/EmptyResponseFactory.kt`
- `app/src/main/java/com/example/videobrowser/adblock/SyntheticResponseFactory.kt`
- `app/src/main/java/com/example/videobrowser/adblock/SyntheticResponseRegistry.kt`
- `app/src/main/java/com/example/videobrowser/adblock/BuiltInAdBlockRules.kt`
- `app/src/main/java/com/example/videobrowser/adblock/AdBlockLogger.kt`

### 页面净化和元素屏蔽

页面加载完成后，应用会通过 `JsInjector` 注入内置脚本。

当前页面增强配置包含：

- 是否允许 JS 注入。
- 是否启用页面净化。
- 是否启用视频增强。
- 规则系统提供的 CSS selector。
- 用户手动选择的 CSS selector。
- 规则系统提供的 DOM remove selector。
- URL contains 阻断关键词。
- 安全 scriptlet 映射出的 `window.open` 阻断关键词。
- 安全 scriptlet 映射出的 `fetch` 阻断关键词。
- 安全 scriptlet 映射出的跳过按钮点击和 video controls 开关。

页面净化主要由 `assets/scripts/common.js` 执行，能力包括：

- 注入 CSS 隐藏规则。
- 删除匹配 DOM 元素。
- 拦截常见广告相关 URL 关键词。
- 尝试点击跳过广告按钮。
- 处理网页 video 元素和全屏增强。
- 消费白名单 scriptlet 映射配置，但不执行规则原文 JavaScript。
- 支持元素选择器回调到 Android 侧保存用户规则。

用户可通过“屏蔽元素”在当前页面选择元素，保存为当前 host 的用户隐藏规则。用户手动规则保存在 `SettingsManager` 中，并可在功能中心管理。

关键代码：

- `app/src/main/java/com/example/videobrowser/inject/PageFeatureCoordinator.kt`
- `app/src/main/java/com/example/videobrowser/inject/JsInjector.kt`
- `app/src/main/java/com/example/videobrowser/inject/ScriptLoader.kt`
- `app/src/main/java/com/example/videobrowser/element/ElementPickerController.kt`
- `app/src/main/assets/scripts/common.js`

### 站点适配脚本

站点适配只从应用内置 `assets/scripts/` 读取本地 JavaScript 文件，不加载远程脚本。

当前默认站点适配：

| 站点 | 匹配域名 | 脚本 |
| --- | --- | --- |
| YouTube | `youtube.com` | `scripts/youtube.js` |
| Bilibili | `bilibili.com` | `scripts/bilibili.js` |
| iQIYI | `iqiyi.com` | `scripts/iqiyi.js` |
| Tencent Video | `v.qq.com` | `scripts/tencent.js` |
| Youku | `youku.com` | `scripts/youku.js` |

站点脚本通过 `window.VideoBrowserSiteAdapters[adapterId].apply(config)` 接收页面增强配置。

关键代码：

- `app/src/main/java/com/example/videobrowser/site/SiteAdapterRegistry.kt`
- `app/src/main/java/com/example/videobrowser/site/SiteAdapter.kt`
- `app/src/main/java/com/example/videobrowser/site/SiteProfile.kt`
- `app/src/main/java/com/example/videobrowser/inject/JsInjector.kt`

### 视频播放和视频手势

项目有两条视频路径：

1. 网页内 video：通过 WebView + 注入脚本 + `FullscreenVideoController` 增强。
2. 直链或本地视频：通过 `PlayerActivity` 使用 Media3 ExoPlayer 播放。

当前视频重构已经按 `refence/iris` 的能力边界完成取舍：参考其播放状态、队列、历史续播、字幕音轨和控制模型，但继续保留 Android WebView + Media3 架构，不迁移 Flutter、MediaKit、FVP 或桌面文件管理能力。

可识别为可播放媒体的 URL 或文件包括：

- 扩展名：`.mp4`、`.m4v`、`.webm`、`.mkv`、`.mov`、`.3gp`、`.ts`、`.mpeg`、`.mpg`、`.flv`、`.m3u8`、`.mpd`
- Scheme：`rtsp`、`rtspt`
- MIME：`video/*`、HLS、DASH、SmoothStreaming 相关 MIME
- URL 中的 `.ism/manifest`

原生播放器能力：

- Media3 ExoPlayer 播放。
- 支持 HTTP 重定向。
- 可传递 User-Agent。
- 可传递 Cookie 和 Referer 请求头。
- 通过 `MediaRoutingController` 集中处理地址栏、WebView URL override、下载监听和本地 SAF 文档的媒体路由。
- 使用 `PlayableMediaItem`、`PlaybackQueue`、`PlaybackSessionState` 和 `PlaybackCommand` 表达原生播放输入、队列、状态和控制命令。
- 按媒体身份保存播放历史：远程媒体用 URL，本地 SAF 文档用 `content://` URI。
- 恢复播放位置和倍速；媒体接近结尾时不自动续播。
- 无痕模式下不写入原生播放历史。
- 可在功能中心查看和清空原生播放历史，也可选择“视频总是从头播放”忽略历史进度。
- 支持单项队列、SAF 同目录 sibling 视频队列、上一项/下一项、顺序播放、单项循环、列表循环、随机播放和恢复顺序。
- 支持队列面板、移除队列项、当前播放项标记。
- 支持本地同名字幕候选自动关联，外部字幕通过 Media3 `SubtitleConfiguration` 挂载。
- 支持 Media3 音轨和字幕轨选择面板。
- 支持画面适应、拉伸和裁剪缩放模式。
- 保存播放位置、播放状态、媒体项索引、横竖屏状态、倍速、repeat、队列和缩放模式。
- 默认横屏，可切换横竖屏。
- 全屏沉浸式隐藏系统栏。
- 视频增强效果失败时会自动重试无效果播放。

手势层能力按代码实现包括：

- 点击唤醒控制层。
- 播放/暂停。
- 快进、快退。
- 拖动进度：未知时长使用 1 分钟 seek span，长视频 seek span 上限为 10 分钟，松手后提交。
- 左侧亮度调节。
- 右侧音量调节。
- 锁定和解锁。
- 横竖屏切换。
- 倍速选择：`0.5x`、`0.75x`、`1x`、`1.25x`、`1.5x`、`2x`、`2.5x`、`3x`。
- 默认倍速持久化。
- 侧边长按扫描：向右临时 `2x`，向左按固定步长回退扫描，松手恢复原倍速和播放状态。
- 队列、repeat、轨道和缩放按钮。

WebView 视频路径保留网页播放器边界：

- `common.js` 暴露 `VideoBrowserEnhancer.seekBy()`、`seekTo()`、`togglePlayPause()`、`setPlaybackSpeed()`、`startDirectionalPlayback()`、`stopDirectionalPlayback()`、`reportPlaybackTimeline()`。
- 站点脚本的 `videoCapabilities` 优先处理站点播放器能力，通用 HTMLVideoElement 操作作为 fallback。
- Kotlin 侧通过 `WebViewVideoCommand` 和 `WebViewVideoTimeline` 类型化 WebView 视频控制脚本和 timeline 数据。
- WebView 视频只控制网页播放器，不写入原生播放队列或原生播放历史。

关键代码：

- `app/src/main/java/com/example/videobrowser/video/MediaRoutingController.kt`
- `app/src/main/java/com/example/videobrowser/video/PlayableMediaItem.kt`
- `app/src/main/java/com/example/videobrowser/video/PlaybackQueue.kt`
- `app/src/main/java/com/example/videobrowser/video/PlaybackSessionState.kt`
- `app/src/main/java/com/example/videobrowser/video/PlaybackHistoryRepository.kt`
- `app/src/main/java/com/example/videobrowser/video/LocalPlaybackQueueBuilder.kt`
- `app/src/main/java/com/example/videobrowser/video/LocalSubtitleMatcher.kt`
- `app/src/main/java/com/example/videobrowser/video/WebViewVideoProtocol.kt`
- `app/src/main/java/com/example/videobrowser/video/PlayerActivity.kt`
- `app/src/main/java/com/example/videobrowser/video/FullscreenVideoController.kt`
- `app/src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt`
- `app/src/main/java/com/example/videobrowser/video/VideoGestureFeedbackFormatter.kt`
- `app/src/main/java/com/example/videobrowser/video/VideoSeekDragCalculator.kt`
- `app/src/main/java/com/example/videobrowser/video/VideoSpeedOptions.kt`
- `app/src/main/java/com/example/videobrowser/functioncenter/PlaybackHistoryPage.kt`
- `app/src/main/java/com/example/videobrowser/utils/MediaUrlUtils.kt`

### 下载

下载由 `DownloadController` 接入 WebView 下载监听。

处理逻辑：

- 如果下载 URL 可识别为可播放媒体，优先进入原生播放器。
- 否则使用 Android `DownloadManager` 下载到公共下载目录。
- 下载请求会携带当前 User-Agent。
- 如果 WebView 中存在该 URL 的 Cookie，会作为请求头传给 DownloadManager。
- 下载记录保存到 `DownloadRecordRepository`。
- 功能中心提供下载记录页面、下载记录搜索、按状态/类型筛选、进行中状态/进度刷新、进行中下载取消、单条下载记录删除、来源链接复制、下载文件打开/分享和清空下载记录操作。

关键代码：

- `app/src/main/java/com/example/videobrowser/download/DownloadController.kt`
- `app/src/main/java/com/example/videobrowser/download/DownloadRecord.kt`
- `app/src/main/java/com/example/videobrowser/download/DownloadRecordRepository.kt`
- `app/src/main/java/com/example/videobrowser/download/DownloadRecordCleaner.kt`
- `app/src/main/java/com/example/videobrowser/functioncenter/DownloadsPage.kt`

### 本地文件管理

本地文件使用 Android Storage Access Framework，不直接申请通用存储权限。

当前能力：

- 选择并打开单个本地文件。
- 授权一个目录。
- 记住上次授权目录。
- 浏览授权目录。
- 创建文件夹。
- 创建 `.txt` 文本文件。
- 打开文件。
- 分享文件。
- 重命名文件。
- 删除文件。
- 视频文件或可播放媒体 URI 进入原生播放器，其他文件交给系统应用打开。

关键代码：

- `app/src/main/java/com/example/videobrowser/localfiles/LocalFilesController.kt`
- `app/src/main/java/com/example/videobrowser/localfiles/LocalFileLaunchers.kt`
- `app/src/main/java/com/example/videobrowser/localfiles/LocalDirectoryPermissionManager.kt`
- `app/src/main/java/com/example/videobrowser/localfiles/LocalDocumentRepository.kt`
- `app/src/main/java/com/example/videobrowser/localfiles/LocalDocument.kt`
- `app/src/main/java/com/example/videobrowser/localfiles/LocalDocumentFormatter.kt`

### 功能中心

功能中心是项目的主要工具入口。

根功能入口按代码包含：

- 分享页面。
- 收藏夹。
- 历史记录。
- 下载。
- 文件操作。
- 刷新。
- 收藏当前页。
- 屏蔽元素。
- 更多。

配置和管理页面包括：

- 当前站点设置。
- 浏览器设置。
- 站点配置。
- 广告拦截日志。
- 用户白名单。
- 手动屏蔽规则。
- 规则订阅。
- 收藏夹管理。
- 浏览历史管理。
- 下载记录管理。
- Cookie 管理。
- 缓存管理。
- 站点数据管理（支持按域名搜索）。
- 恢复默认设置。
- 关于页。

无痕模式下，部分会写入持久化数据的入口会隐藏或禁用。

关键代码：

- `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterController.kt`
- `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt`
- `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionCatalog.kt`
- `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterProfileActionCatalog.kt`
- `app/src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt`
- `app/src/main/java/com/example/videobrowser/functioncenter/CurrentSiteSettingsPage.kt`
- `app/src/main/java/com/example/videobrowser/functioncenter/AboutPage.kt`

### 无痕模式

无痕模式是运行态模式，不持久化保存。`SettingsManager.isPrivateBrowsingEnabled()` 会移除旧的持久化 key 并返回默认值 `false`。

进入无痕模式时：

- 创建临时 WebView。
- 切换 `BrowserManager` 到临时 WebView。
- 禁用 Cookie。
- 禁用 DOM Storage。
- 禁用 WebView database。
- 使用 `LOAD_NO_CACHE`。
- 不写入历史记录。

退出无痕模式时：

- 清理临时浏览数据。
- 销毁临时 WebView。
- 切回标准 WebView。

关键代码：

- `app/src/main/java/com/example/videobrowser/browser/BrowserSessionCoordinator.kt`
- `app/src/main/java/com/example/videobrowser/browser/RuntimePrivateBrowsingState.kt`
- `app/src/main/java/com/example/videobrowser/browser/BrowserManager.kt`
- `app/src/main/java/com/example/videobrowser/browser/PageActionsController.kt`

## 技术栈

| 类别 | 当前配置 |
| --- | --- |
| 语言 | Kotlin |
| 构建脚本 | Gradle Kotlin DSL |
| Gradle Wrapper | Gradle `9.4.1` |
| Android Gradle Plugin | `9.2.1` |
| Java 编译目标 | Java 11 |
| Android namespace | `com.example.videobrowser` |
| applicationId | `com.example.videobrowser` |
| minSdk | 24 |
| targetSdk | 36 |
| compileSdk | Android 36，minor API 1 |
| UI | AppCompat、Material、ConstraintLayout、XML + Kotlin 动态 UI |
| 网页容器 | Android WebView |
| 播放器 | AndroidX Media3 ExoPlayer |
| 本地测试 | JUnit 4 |
| Android 测试 | AndroidX Test、Espresso |

主要依赖版本集中在：

```text
gradle/libs.versions.toml
```

## 目录结构

```text
VideoBrowser/
  app/
    build.gradle.kts
    proguard-rules.pro
    src/
      main/
        AndroidManifest.xml
        java/com/example/videobrowser/
        assets/
          rules/
          scripts/
        res/
      test/
      androidTest/
  gradle/
    libs.versions.toml
    wrapper/
  build.gradle.kts
  settings.gradle.kts
  gradle.properties
  gradlew
  gradlew.bat
```

核心代码包：

| 路径 | 职责 |
| --- | --- |
| `com.example.videobrowser` | 主 Activity 和 View 绑定 |
| `browser/` | WebView 管理、页面状态、请求上下文、地址栏、外部打开 |
| `browser/search/` | 搜索入口、地址建议、远程建议解析 |
| `adblock/` | 广告请求拦截策略、拦截器、日志、内置请求规则 |
| `adguard/` | AdGuard / EasyList 风格文本规则的安全子集解析入口 |
| `rules/` | 请求规则、CSS 规则、DOM 规则、编译、索引、匹配 |
| `inject/` | 本地 JavaScript 加载和注入 |
| `site/` | 站点适配器注册和 host 匹配 |
| `video/` | 原生播放器、网页全屏视频控制、手势覆盖层 |
| `download/` | 系统下载器接入和下载记录 |
| `localfiles/` | 本地文件和授权目录管理 |
| `functioncenter/` | 功能中心页面和操作入口 |
| `settings/` | 设置、白名单、站点例外、自定义快捷入口 |
| `storage/` | SharedPreferences 抽象、收藏和历史 |
| `element/` | 页面元素选择器 |
| `utils/` | URL 和媒体 URL 工具 |
| `version/` | 版本号格式化 |

## 构建和运行

### 前置条件

- Android Studio 或 Android SDK 命令行环境。
- JDK 11 或兼容 Android Gradle Plugin 9.2.1 的 JDK。
- 本机 `local.properties` 中需要有 Android SDK 路径，例如：

```properties
sdk.dir=C\:\\Users\\<user>\\AppData\\Local\\Android\\Sdk
```

### Android Studio

1. 用 Android Studio 打开项目根目录。
2. 等待 Gradle 同步完成。
3. 选择 `app` 运行配置。
4. 连接 Android 设备或启动模拟器。
5. 点击 Run 安装运行。

### 命令行

Windows PowerShell：

```powershell
.\gradlew.bat assembleDebug
```

安装到已连接设备：

```powershell
.\gradlew.bat installDebug
```

运行本地单元测试：

```powershell
.\gradlew.bat testDebugUnitTest
```

运行 Android 仪器测试需要设备或模拟器：

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## 版本号规则

版本号由 Git 提交数生成。

`app/build.gradle.kts` 中的逻辑：

- `versionCode = gitCommitCount.coerceAtLeast(1)`
- `versionName = formatCommitCountVersion(gitCommitCount)`
- `BuildConfig.GIT_COMMIT_COUNT` 写入当前提交数。

格式化规则：

```text
commitCount = 1234 -> versionName = 1.2.3.4
commitCount = 45   -> versionName = 0.0.4.5
```

关于页显示的是 `BuildConfig.VERSION_NAME`。

相关代码：

- `app/build.gradle.kts`
- `app/src/main/java/com/example/videobrowser/version/AppVersionFormatter.kt`
- `app/src/main/java/com/example/videobrowser/functioncenter/AboutPage.kt`

## Android Manifest 和权限

Manifest 当前声明：

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

注册的 Activity：

- `.MainActivity`：启动入口，`exported="true"`。
- `.video.PlayerActivity`：原生播放器页面，`exported="false"`。

应用配置：

- `android:allowBackup="true"`
- `android:networkSecurityConfig="@xml/network_security_config"`
- `android:hardwareAccelerated="true"`
- 支持 RTL。
- 应用图标使用 `@mipmap/app_logo` 和 `@mipmap/app_logo_round`。

网络安全配置当前允许明文流量：

```xml
<base-config cleartextTrafficPermitted="true" />
```

相关文件：

- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/xml/network_security_config.xml`

## 规则系统

规则来源包括：

1. 代码内置请求规则：`BuiltInAdBlockRules`
2. assets 规则文件：
   - `app/src/main/assets/rules/request_rules.txt`
   - `app/src/main/assets/rules/css_rules.txt`
   - `app/src/main/assets/rules/dom_rules.txt`
   - `app/src/main/assets/rules/scriptlet_rules.txt`
   - `app/src/main/assets/rules/removeparam_rules.txt`
3. 本地缓存目录：
   - `filesDir/rules/request_rules.txt`
   - `filesDir/rules/css_rules.txt`
   - `filesDir/rules/dom_rules.txt`
   - `filesDir/rules/scriptlet_rules.txt`
   - `filesDir/rules/removeparam_rules.txt`
   - `filesDir/rules/metadata.properties`

`RuleEngineFactory` 会加载 assets 和缓存目录中的规则，并记录被跳过的规则。浏览器设置中的“规则订阅”页面支持从 URL 下载规则文本，或手动粘贴规则文本导入。导入时会按当前安全子集拆分为请求规则、CSS 规则、scriptlet 规则和 URL 参数清理规则，并写入本地缓存；更新失败时保留上一份可用缓存。

### 请求规则支持范围

当前支持的是安全子集，不是完整 AdGuard / EasyList / uBlock 语法。

支持：

- `||domain^` 形式的域名规则。
- 普通 URL contains 规则。
- 带 `*`、`^`、起始 `|`、结尾 `|` 的 URL pattern。
- `@@` 放行规则。
- `$domain=` 作用域。
- `$third-party` / `$3p`。
- `$~third-party` / `$1p` / `$first-party`。
- `$redirect=noopjs`、`$redirect=noopcss`、`$redirect=nooptext`，只映射到项目内置 noop 响应。
- 安全资源类型选项：
  - `document`
  - `script`
  - `image`
  - `stylesheet`
  - `css`
  - `media`
  - `font`
  - `object`
  - `subdocument`
  - `frame`
  - `xmlhttprequest`
  - `xhr`
  - `fetch`
  - `ping`
  - `beacon`
  - `other`

不支持或会跳过：

- 包含 `##`、`#@#`、`#%#` 的请求规则。
- 未识别的请求规则 option。
- 反向资源类型选项，例如 `$~script`。
- 完整 uBlock / AdGuard 规则语义。
- 任意远程 redirect 资源或未知 redirect 资源名。
- 规则原文 JavaScript 执行。

### URL 参数清理规则支持范围

`$removeparam` 规则只作用于主框架导航 URL，不处理子资源请求参数。

支持：

- `||domain^$removeparam=parameter_name`
- `||domain^$removeparam=parameter_name,domain=include.example|~exclude.example`

安全限制：

- 只支持精确参数名，参数名长度 1-64。
- 参数名只能包含字母、数字、`.`、`_`、`~`、`-`。
- 不支持 `$removeparam` 正则、空参数名、任意脚本或子资源参数重写。

### CSS 规则支持范围

支持：

- 全局隐藏：`##selector`
- 站点隐藏：`example.com##selector`
- CSS 例外：`example.com#@#selector`
- 多域名作用域：`a.com,b.com##selector`
- 排除域名：`a.com,~b.com##selector`

选择器安全限制：

- selector 不能为空。
- selector 长度不能超过 200。
- selector 不能包含 `{`、`}`、`;`、`<`、`>`。
- selector 不能包含 `:has(`、`:contains(`、`:matches(`、`:xpath(`、`javascript:`、`expression(`。

不支持：

- `#%#`
- `#?#`
- 不安全选择器。

### DOM 删除规则支持范围

DOM 删除规则格式：

```text
remove:selector
```

当前 `dom_rules.txt` 解析为全局规则，不支持在 DOM 删除规则行内声明域名作用域。

### 安全 scriptlet 规则支持范围

scriptlet 规则来自 `scriptlet_rules.txt`，只映射到项目已有的本地 Hook，不拼接或执行规则原文 JavaScript。

支持语法：

```text
example.com##+js(window-open-block-keyword, /popup-ad/)
example.com#%#//scriptlet('fetch-block-keyword', '/pagead/')
```

当前白名单 scriptlet：

- `window-open-block-keyword`：参数为安全关键词，映射到 `window.open` URL 关键词阻断。
- `fetch-block-keyword`：参数为安全关键词，映射到 `fetch` URL 关键词阻断。
- `click-skip-buttons`：无参数，允许在视频增强关闭时单独运行跳过按钮点击。
- `enable-video-controls`：无参数，允许在视频增强关闭时单独启用 video controls。

会跳过：

- 未知 scriptlet 名称。
- 参数数量或内容不合法的 scriptlet。
- 域名作用域不合法的 scriptlet。
- 原始 `#%#` JavaScript。

### 规则索引

规则编译后会建立索引：

- 请求域名 host / suffix 索引。
- URL contains 关键词索引。
- CSS / DOM 页面 host 索引。
- 全局 fallback。

关键代码：

- `app/src/main/java/com/example/videobrowser/rules/Rule.kt`
- `app/src/main/java/com/example/videobrowser/rules/RemoveParamRule.kt`
- `app/src/main/java/com/example/videobrowser/rules/ElementRule.kt`
- `app/src/main/java/com/example/videobrowser/rules/ScriptletRule.kt`
- `app/src/main/java/com/example/videobrowser/rules/ScriptletRegistry.kt`
- `app/src/main/java/com/example/videobrowser/adguard/AdGuardRuleParser.kt`
- `app/src/main/java/com/example/videobrowser/rules/RuleSubscriptionImporter.kt`
- `app/src/main/java/com/example/videobrowser/rules/RuleFileLoader.kt`
- `app/src/main/java/com/example/videobrowser/rules/RuleCompiler.kt`
- `app/src/main/java/com/example/videobrowser/rules/RuleEngine.kt`
- `app/src/main/java/com/example/videobrowser/rules/RequestRuleIndex.kt`
- `app/src/main/java/com/example/videobrowser/rules/ElementRuleIndex.kt`

## 数据存储

主要本地存储文件：

```text
browser_preferences
```

由 `PreferenceStore.from(context)` 创建。

当前保存的数据包括：

- 全局广告拦截开关。
- JS 注入开关。
- 页面净化开关。
- 视频增强开关。
- 默认视频倍速。
- 首页 URL。
- 搜索引擎 ID。
- 自定义快捷入口。
- 桌面模式。
- 当前站点关闭广告拦截列表。
- 当前站点关闭 JS 注入列表。
- 当前站点关闭页面净化列表。
- 当前站点关闭视频增强列表。
- 用户白名单。
- 用户手动元素隐藏规则。
- 收藏。
- 历史。
- 收藏和历史的创建/更新时间。
- 下载记录。
- 原生播放历史。
- 已授权本地目录 URI。

收藏和历史限制：

- 收藏最多 500 条。
- 历史最多 1000 条。

自定义快捷入口限制：

- 最多 10 条。
- URL 必须是 `http` 或 `https`。

相关代码：

- `app/src/main/java/com/example/videobrowser/storage/PreferenceStore.kt`
- `app/src/main/java/com/example/videobrowser/storage/SavedPageRepository.kt`
- `app/src/main/java/com/example/videobrowser/settings/SettingsManager.kt`
- `app/src/main/java/com/example/videobrowser/download/DownloadRecordRepository.kt`
- `app/src/main/java/com/example/videobrowser/video/PlaybackHistoryRepository.kt`
- `app/src/main/java/com/example/videobrowser/localfiles/LocalDirectoryPermissionManager.kt`

## 默认设置

按 `SettingsManager` 当前代码：

| 设置项 | 默认值 |
| --- | --- |
| 搜索引擎 | `baidu` |
| 首页 | `https://m.baidu.com/` |
| 默认视频倍速 | `1f` |
| 视频总是从头播放 | 关闭 |
| 广告请求拦截 | 开启 |
| 网页脚本增强 | 开启 |
| 页面净化 | 开启 |
| 视频增强 | 开启 |
| 桌面模式 | 关闭 |
| 无痕模式 | 关闭，且不持久化 |

“恢复默认设置”会清理设置、站点例外、用户白名单、用户手动屏蔽规则、自定义快捷入口、收藏、历史、缓存、Cookie 和站点数据，并重新创建 Activity。

## 新增和维护指南

### 新增搜索入口

修改：

```text
app/src/main/java/com/example/videobrowser/browser/search/SearchProvider.kt
```

在 `SearchProviders.defaults` 中添加 `SearchProvider`。

如果需要专用远程建议接口，还要修改：

```text
app/src/main/java/com/example/videobrowser/browser/search/SearchSuggestionClient.kt
```

### 新增请求拦截规则

修改内置规则文件：

```text
app/src/main/assets/rules/request_rules.txt
```

或修改代码内置规则：

```text
app/src/main/java/com/example/videobrowser/adblock/BuiltInAdBlockRules.kt
```

新增后应运行：

```powershell
.\gradlew.bat testDebugUnitTest
```

重点关注 `rules/` 和 `adblock/` 下的测试。

### 新增页面隐藏或删除规则

CSS 隐藏规则：

```text
app/src/main/assets/rules/css_rules.txt
```

DOM 删除规则：

```text
app/src/main/assets/rules/dom_rules.txt
```

注意选择器安全限制，复杂或高风险 selector 会被跳过。

### 新增站点适配

1. 在 `app/src/main/assets/scripts/` 新增站点脚本，例如 `example.js`。
2. 在 `SiteAdapterRegistry.default()` 中注册域名和脚本路径。
3. 站点脚本挂载到 `window.VideoBrowserSiteAdapters[adapterId]`。
4. 实现 `apply(config)`。

相关文件：

```text
app/src/main/java/com/example/videobrowser/site/SiteAdapterRegistry.kt
app/src/main/assets/scripts/
```

脚本路径必须满足 `ScriptLoader` 的校验：

- 位于 `scripts/` 目录下。
- 以 `.js` 结尾。
- 不能包含反斜杠。
- 不能包含 `..` 或空路径段。

### 新增功能中心页面

通常需要修改：

```text
app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterPages.kt
app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterRootActionCatalog.kt
app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterProfileActionCatalog.kt
app/src/main/res/values/strings.xml
```

页面 UI 当前主要由 Kotlin 动态创建，公共行、分区、空状态等能力在 `FunctionCenterController` 和 `FunctionCenterPageHost` 中。

### 新增设置项

通常需要修改：

```text
app/src/main/java/com/example/videobrowser/settings/SettingsManager.kt
app/src/main/java/com/example/videobrowser/functioncenter/BrowserSettingsPage.kt
app/src/main/java/com/example/videobrowser/functioncenter/CurrentSiteSettingsPage.kt
app/src/main/res/values/strings.xml
```

如果设置项需要“恢复默认设置”清理，要加入 `SettingsManager.RESET_KEYS`。

### 修改原生播放器能力

主要修改：

```text
app/src/main/java/com/example/videobrowser/video/PlayerActivity.kt
app/src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt
app/src/main/java/com/example/videobrowser/video/VideoGestureFeedbackFormatter.kt
app/src/main/java/com/example/videobrowser/video/PlaybackSessionState.kt
app/src/main/java/com/example/videobrowser/video/PlaybackQueue.kt
app/src/main/java/com/example/videobrowser/video/WebViewVideoProtocol.kt
```

如果涉及可识别媒体类型，修改：

```text
app/src/main/java/com/example/videobrowser/utils/MediaUrlUtils.kt
app/src/main/java/com/example/videobrowser/video/MediaRoutingController.kt
```

## 测试

本地单元测试目录：

```text
app/src/test/java/com/example/videobrowser/
```

Android 仪器测试目录：

```text
app/src/androidTest/java/com/example/videobrowser/
```

当前测试覆盖的主要方向：

- 布局契约。
- URL 工具。
- 搜索建议解析和排序。
- 请求上下文和资源类型推断。
- 广告请求策略。
- 规则解析、编译、索引、性能。
- AdGuard 安全子集解析、订阅导入、缓存回退和 URL 参数清理。
- JS 注入脚本拼接。
- 设置管理。
- 收藏、历史和下载记录。
- 原生播放历史、续播、播放队列和本地字幕候选。
- 功能中心页面和入口顺序。
- 无痕状态。
- 原生播放器状态/命令、队列、轨道、缩放、进度拖动、倍速和网页视频协议契约。
- 广告拦截和 JS 注入的 Android 环境测试。
- G6 回归集合：广告决策、noop redirect 边界、站点开关、用户白名单、规则 cache 回退、页面功能配置、WebView 视频 controls/倍速/fullscreen 事件状态。

常用命令：

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat assembleDebug
```

G6 最近一次验证记录为 2026-06-09：

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat assembleDebugAndroidTest
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.videobrowser.regression.VideoPlaybackRegressionInstrumentedTest"
.\gradlew.bat connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.videobrowser.inject.JsInjectorInstrumentedTest,com.example.videobrowser.adblock.AdBlockRequestInterceptorInstrumentedTest"
```

上述仪器测试在 `Pixel_9a(AVD) - 16` 上通过。视频回归测试覆盖的是 `common.js` 在 WebView 中对 controls、倍速和 fullscreen 事件状态的处理，不等同于真实原生全屏 UI 手工验收。

## 当前边界和非目标

按当前代码，以下能力不是项目已实现目标：

- 不实现完整 uBlock Origin 引擎。
- 不完整兼容 EasyList / AdGuard / uBlock 所有语法。
- 不执行订阅规则或远程规则中的任意 JavaScript。
- 不支持任意远程 redirect 资源。
- 不保证拦截所有广告请求。
- 不保证处理服务端拼接广告、DRM、加密流或站点强绑定播放器逻辑。
- 不提供后端同步、账号系统或云端规则服务。
- 不把无痕模式作为持久设置保存。

## 文档归档

本次归档后，`README.md` 作为项目当前状态、功能边界、构建和测试入口。

历史设计稿、阶段执行计划、进度流水账和重复的代码解析文档已经移除。判断当前行为是否已经实现，应以源码、资源文件和 Gradle 配置为准。
