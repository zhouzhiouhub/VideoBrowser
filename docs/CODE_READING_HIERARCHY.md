# VideoBrowser 代码阅读层级表

这份文档用于按层级阅读项目代码。建议不要从某个大文件硬读到底，而是先按“入口接线 -> 核心控制器 -> 业务模块 -> 数据存储 -> 测试验证”的顺序建立整体地图，再进入具体函数。

## 阅读顺序总览

| 阅读层级 | 先读目标 | 主要文件或目录 | 读完应理解什么 | 下一步 |
| --- | --- | --- | --- | --- |
| 0. 构建和声明层 | 项目如何被 Android/Gradle 识别 | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml` | 这是单模块 Android 应用；入口 Activity、播放器 Activity、权限、主题和网络安全声明在哪里 | 进入主界面入口 |
| 1. 主入口接线层 | App 启动后如何把模块连接起来 | `MainActivity.kt`, `MainActivityViews.kt`, `MainActivityBrowsingModeTheme.kt`, `res/layout/activity_main.xml` | 主界面控件、WebView、搜索、标签、拦截、注入、下载、播放器、功能中心如何被创建和绑定 | 进入浏览器核心 |
| 2. 浏览器核心层 | WebView 如何加载网页和维护状态 | `browser/` | 页面加载、前进后退、错误页、权限、弹窗、标签页、地址栏状态、页面工具如何工作 | 进入搜索/站点/增强 |
| 3. 搜索和首页层 | 地址栏如何从输入变成网址或搜索 | `browser/search/`, `BrowserLaunchController.kt`, `BrowserSessionController.kt` | 默认搜索引擎、地址建议和 App 自定义首页状态如何协作 | 回到浏览器加载流程 |
| 4. 站点适配层 | 不同视频网站的差异如何处理 | `site/`, `assets/scripts/*.js` | Kotlin 侧如何识别站点能力，JS 侧如何处理站点播放器结构 | 进入脚本注入 |
| 5. 内容增强层 | 广告拦截、页面净化和元素屏蔽如何串起来 | `adblock/`, `adguard/`, `rules/`, `inject/`, `element/`, `assets/scripts/common.js` | 请求拦截、规则解析、JS 注入、DOM 清理、元素选择器如何协作 | 进入视频能力 |
| 6. 视频播放层 | 网页视频和原生播放器如何互通 | `video/`, `browser/VideoBrowserNativeBridge.kt`, `assets/scripts/common.js` | 网页视频桥接、全屏手势、原生播放器、播放队列、字幕、播放历史如何实现 | 进入数据和功能中心 |
| 7. 功能中心层 | 用户入口和设置页面如何搭建 | `functioncenter/` | 底部功能面板、页面栈、设置页、下载页、历史/收藏页、规则页如何由 Kotlin 动态生成 | 进入数据层 |
| 8. 数据和持久化层 | 本地数据如何保存和恢复 | `storage/`, `settings/`, `download/`, `localfiles/` | SharedPreferences 封装、设置读写、收藏历史、下载记录、本地文件授权如何实现 | 读测试确认行为 |
| 9. 工具和边界层 | URL、媒体地址、版本等公共逻辑在哪里 | `utils/`, `version/` | 可复用的纯函数和格式化逻辑如何支撑上层功能 | 进入测试层 |
| 10. 测试验证层 | 哪些行为被契约测试固定 | `app/src/test/java/`, `app/src/androidTest/java/` | 每个功能的预期行为、禁止回退点、关键字符串契约和回归场景 | 回到对应实现文件修改 |

## 功能实现层级表

| 功能 | 第一入口 | 核心实现 | 数据/配置 | 相关测试 | 阅读建议 |
| --- | --- | --- | --- | --- | --- |
| App 启动和界面初始化 | `MainActivity.onCreate()` | `MainActivityViews.bind()`, `MainActivityBrowsingModeTheme.colors()` | `res/layout/activity_main.xml`, `res/values/*.xml` | `MainActivityLayoutContractTest`, `AndroidManifestContractTest` | 先看 `onCreate()` 里模块创建顺序，再看每个控制器构造参数 |
| WebView 创建和设置 | `MainActivity.createBrowserManager()` 附近逻辑 | `browser/BrowserManager.kt` | `settings/SettingsManager.kt` | `BrowserManagerWebSettingsContractTest` | 重点看 WebSettings、WebViewClient、WebChromeClient 如何被注入 |
| 页面加载和导航 | `MainActivity.loadUrl()`, `BrowserManager.load()` | `BrowserClient.kt`, `BrowserSessionController.kt`, `HistoryRecordPolicy.kt` | `BrowserTabSessionRepository.kt` | `BrowserClientContractTest`, `HistoryRecordPolicyTest` | 从地址输入到 `BrowserRequest`，再到 WebView 回调 |
| 前进/后退/刷新/停止 | `MainActivity` 底部按钮监听 | `BrowserControlsController.kt`, `BrowserControlsScrollController.kt` | 无独立存储 | `BrowserControlsControllerContractTest`, `BottomBarButtonVisibilityTest` | 先看按钮可见性和启用状态，再看点击动作 |
| 标签页管理 | `MainActivity` 标签相关 region | `BrowserTabStore.kt`, `BrowserTabWebViewRegistry.kt`, `BrowserSessionCoordinator.kt` | `BrowserTabSessionRepository.kt` | `BrowserTabStoreTest`, `BrowserTabWebViewRegistryTest`, `BrowserTabSessionRepositoryTest` | 按“标签模型 -> WebView 绑定 -> 会话恢复”阅读 |
| 新窗口和网页弹窗 | `ChromeClient.onCreateWindow()` | `ChromeClient.kt`, `BrowserTabWebViewRegistry.kt` | 标签页会话存储 | `WebViewNewWindowContractTest`, `BrowserTabWebViewWiringContractTest` | 关注用户手势、弹窗 WebView 绑定和关闭请求 |
| 地址栏搜索 | `SearchProviderController` 入口 | `browser/search/SearchProviderController.kt`, `SearchProviders` | `SettingsManager` 默认搜索引擎和自定义搜索引擎 | `SearchProviderControllerContractTest` | 先看搜索引擎定义，再看地址栏 badge、搜索 URL 和功能中心搜索引擎页面如何复用同一 provider |
| App 自定义首页 | `BrowserLaunchController.openHomePage()` | `BrowserLaunchController.kt`, `BrowserSessionController.kt`, `BrowserShellUiController.kt` | 当前会话状态 | `BrowserLaunchControllerContractTest`, `MainActivityLayoutContractTest` | 看主页入口如何 reset 会话，而不是加载搜索引擎首页 |
| 地址建议 | 地址输入监听 | `AddressSuggestionController.kt`, `AddressSuggestionRanker.kt` | 收藏、历史、远程建议 | `AddressSuggestionRankerTest` | 按“候选来源 -> 排序 -> UI 展示”阅读 |
| 站点安全状态 | 页面 URL 变化后刷新 | `SiteSecurityStatus.kt`, `MainActivity.updateSiteSecurityStatus()` | WebView/URL 状态 | `SiteSecurityStatusTest` | 看 HTTPS/HTTP/about/file 等状态如何映射到图标和文案 |
| 网页权限 | `ChromeClient` 权限回调 | `MainActivity` 权限 region, `SessionSitePermissionStore.kt` | `SettingsManager` 站点权限 | `WebPermissionRequestContractTest`, `WebGeolocationPermissionContractTest` | 按“网页请求 -> Android 权限 -> 站点决策 -> 回调网页”阅读 |
| 文件选择和客户端证书 | `ChromeClient` 回调到 `MainActivity` | `MainActivity` 文件选择/证书 region | Android 系统选择器 | `WebFileChooserContractTest` | 重点看 pending callback 如何保存和释放 |
| HTTP Basic Auth | `BrowserClient` 回调 | `MainActivity` auth region | 无长期存储 | `BrowserClientContractTest` | 读弹窗创建、取消和回调处理 |
| 错误页和安全导航 | `BrowserClient` 错误回调 | `BrowserErrorPage.kt`, `HttpNavigationSafetyPolicy.kt`, `LocalWebArchivePolicy.kt` | 无独立存储 | `BrowserErrorPageTest`, `HttpNavigationSafetyPolicyTest` | 看哪些 URL 可以展示错误页、哪些导航需要确认 |
| 页面工具 | 功能中心或按钮触发 | `PageActionsController.kt`, `PageArchiveFileName.kt`, `FindInPageController.kt` | MHTML 临时文件、系统打印 | `PrintPageWiringContractTest`, `SavePageArchiveWiringContractTest`, `FindInPageControllerTest` | 先看工具入口，再看每个工具对 WebView 的调用 |
| 请求广告拦截 | `BrowserClient.shouldInterceptRequest()` | `AdBlockRequestInterceptor.kt`, `AdBlockRequestPolicy.kt`, `RuleDecisionResolver.kt` | `SettingsManager`, 规则引擎 | `AdBlockRequestPolicyTest`, `AdBlockRequestInterceptorInstrumentedTest` | 按“请求上下文 -> 策略判断 -> 合成响应”阅读 |
| 内置广告规则 | 拦截策略调用 | `BuiltInAdBlockRules.kt`, `Rule.kt`, `RuleMatcher.kt` | 内置列表和订阅规则 | `BuiltInAdBlockRulesTest`, `RuleMatcher` 相关测试 | 看规则如何判断域名、路径、资源类型 |
| AdGuard 规则解析 | 规则导入/加载 | `adguard/AdGuardRuleParser.kt`, `rules/RuleCompiler.kt` | 订阅文本、用户规则 | `AdGuardRuleParserTest`, `RuleCompilerTest` | 按“文本行 -> 规则对象 -> 索引能力”阅读 |
| 规则文件加载和订阅 | 功能中心规则页 | `RuleFileLoader.kt`, `RuleSubscriptionFetcher.kt`, `RuleSubscriptionImporter.kt`, `RuleEngineFactory.kt` | SharedPreferences 和本地文件 | `RuleFileLoaderTest`, `RuleSubscriptionFetcherTest`, `RuleSubscriptionImporterTest` | 重点看失败兜底和大文件索引 |
| JS 注入 | 页面加载完成后触发 | `JsInjector.kt`, `ScriptLoader.kt`, `PageFeatureCoordinator.kt` | `assets/scripts/common.js`, 站点脚本 | `JsInjectorTest`, `PageFeatureCoordinatorTest`, `JsInjectorInstrumentedTest` | 先看 Kotlin 拼配置，再看 JS 如何消费配置 |
| 页面净化和 DOM 清理 | JS 注入后执行 | `assets/scripts/common.js` | 规则选择器、用户选择器 | `ElementPickerScriptContractTest`, `AdBlockRegressionTest` | 在 JS 中按函数注释读：选择器安全检查 -> 删除元素 -> MutationObserver |
| 元素选择器 | 功能中心“屏蔽元素”入口 | `ElementPickerController.kt`, `assets/scripts/common.js` | 用户手动规则 | `ElementPickerScriptContractTest` | 按“开始选择 -> 生成 selector -> 保存规则 -> 重新注入”阅读 |
| 站点视频适配 | JS adapter 注册 | `site/SiteAdapterRegistry.kt`, `assets/scripts/bilibili.js`, `youtube.js`, `iqiyi.js`, `tencent.js`, `youku.js` | 站点域名能力 | `SiteAdapterRegistryTest`, `VideoCapabilityDelegationContractTest` | 先看 Kotlin 识别站点，再看 JS adapter 暴露哪些能力 |
| 网页视频桥接 | JS 调原生桥 | `VideoBrowserNativeBridge.kt`, `WebViewVideoProtocol.kt`, `assets/scripts/common.js` | WebView JS bridge | `VideoBrowserNativeBridgeTest`, `WebViewVideoProtocolTest` | 看 JS 命令如何变成 Kotlin 回调和播放器命令 |
| 网页全屏手势 | 网页视频进入全屏后 | `FullscreenVideoController.kt`, `FullscreenVideoGestureOverlay.kt` | 当前视频时间线状态 | `FullscreenVideoExitButtonContractTest`, `VideoDiagnosticsContractTest` | 按“全屏状态 -> 手势识别 -> 反馈 UI -> JS 控制”阅读 |
| 原生播放器 | 打开媒体地址或本地文件 | `video/PlayerActivity.kt` | Intent extras、保存状态 | `NativePlayback*ContractTest`, `NativeTrackSelectionContractTest` | 先看 `onCreate()`，再看播放器初始化、轨道、缩放、速度、队列 |
| 播放队列 | 原生播放器内部 | `PlaybackQueue.kt`, `LocalPlaybackQueueBuilder.kt` | Intent/本地文件列表 | `PlaybackQueueTest`, `LocalPlaybackQueueBuilderTest` | 按“队列模型 -> 下一集/上一集 -> 随机/重复”阅读 |
| 字幕匹配 | 本地播放时 | `LocalSubtitleMatcher.kt` | 同目录字幕文件 | `LocalSubtitleMatcherTest`, `ExternalSubtitleWiringContractTest` | 看文件名相似度和字幕格式过滤 |
| 播放历史 | 播放过程中记录 | `PlaybackHistoryRepository.kt`, `PlaybackHistoryDisplayText.kt`, `PlaybackHistoryPage.kt` | SharedPreferences | `PlaybackHistoryRepositoryTest`, `PlaybackHistoryPageWiringContractTest` | 按“写入记录 -> 展示文案 -> 功能中心打开”阅读 |
| 下载任务 | WebView 下载或长按下载 | `DownloadController.kt` | Android DownloadManager | `DownloadRetryWiringContractTest`, `DownloadStatusWiringContractTest` | 先看安全策略，再看开始下载和记录同步 |
| 下载记录 | 功能中心下载页 | `DownloadRecordRepository.kt`, `DownloadStatusSynchronizer.kt`, `DownloadsPage.kt` | SharedPreferences | `DownloadRecordRepositoryTest`, `DownloadStatusSynchronizerTest` | 按“记录模型 -> 过滤/搜索 -> 重试/取消/移除”阅读 |
| 下载安全和重试 | 下载前/失败后 | `DownloadSafetyPolicy.kt`, `DownloadRetryPolicy.kt`, `DownloadCancellationPolicy.kt`, `DownloadCanceller.kt` | 下载记录状态 | `DownloadSafetyPolicyTest`, `DownloadRetryPolicyTest`, `DownloadCancellerTest` | 重点看哪些 URL/状态允许继续操作 |
| 功能中心壳层 | 底部功能按钮 | `FunctionCenterController.kt`, `FunctionCenterPageHost.kt`, `FunctionCenterViewFactory.kt` | 页面栈状态 | `FunctionCenterRootSheetLayoutTest`, `FunctionCenterPageHistoryTest` | 按“打开面板 -> 页面栈 -> 动态创建 View”阅读 |
| 功能中心入口目录 | 功能中心首页 | `FunctionCenterPages.kt`, `FunctionCenterRootActionCatalog.kt`, `FunctionCenterProfileActionCatalog.kt` | 当前页面/站点状态 | `FunctionCenterRootActionCatalogTest`, `FunctionCenterProfileActionCatalogTest` | 先看 action catalog，再看每个 action 调到哪里 |
| 浏览器设置页 | 功能中心设置入口 | `BrowserSettingsPage.kt`, `SearchEngineSettingsPage.kt`, `CurrentSiteSettingsPage.kt`, `RestoreDefaultSettingsPage.kt` | `SettingsManager.kt` | `BrowserSettingsPageContractTest`, `CurrentSiteSettingsPageContractTest` | 按“页面项 -> 设置读写函数 -> 刷新 WebView”阅读 |
| 数据管理页 | 功能中心数据入口 | `BrowserDataManagementPage.kt`, `SavedPagesPage.kt`, `BrowserTabsPage.kt`, `SitePermissionsPage.kt` | 收藏、历史、标签、权限存储 | 对应 `functioncenter/*ContractTest` | 先看页面构建，再跳到 repository |
| 关于页 | 功能中心关于入口 | `AboutPage.kt`, `AppVersionFormatter.kt` | BuildConfig 版本信息 | `AboutPageContractTest`, `AppVersionFormatterTest` | 看页面如何避免直接耦合构建细节 |
| 收藏和历史 | 收藏按钮/功能中心 | `SavedPageRepository.kt`, `SavedPageSearch.kt` | SharedPreferences | `SavedPageRepositoryTest`, `SavedPageSearchTest` | 按“保存模型 -> 搜索/分组 -> 导入导出”阅读 |
| 设置存储 | 所有设置读写入口 | `SettingsManager.kt`, `PreferenceStore.kt` | SharedPreferences | `SettingsManagerTest`, `BrowserDefaultSettingsResetterTest` | 先看 key/default，再看站点级集合如何读写 |
| 本地文件 | 功能中心本地文件入口 | `LocalFilesController.kt`, `LocalDocumentRepository.kt`, `LocalDirectoryPermissionManager.kt`, `LocalFileLaunchers.kt` | Android SAF 授权 URI | 本地文件 wiring 测试 | 按“申请目录权限 -> 枚举文档 -> 打开/播放”阅读 |
| URL 和媒体工具 | 多处调用 | `UrlUtils.kt`, `MediaUrlUtils.kt` | 无状态纯函数 | `UrlUtilsTest`, `MediaUrlUtilsTest` | 适合最后读，用来理解 URL 清洗和媒体判断边界 |

## 包目录职责树

```text
com.example.videobrowser
├── MainActivity.kt                  # App 主入口，负责把各模块接到 Activity 生命周期和 UI 事件
├── MainActivityViews.kt             # activity_main.xml 的 View 绑定清单
├── MainActivityBrowsingModeTheme.kt # 普通/无痕模式的主界面配色选择
├── browser/                         # WebView、导航、标签页、权限、页面工具、浏览器 UI 控制
│   └── search/                      # 搜索引擎、地址建议、地址栏搜索源标识
├── site/                            # 站点识别和站点能力描述
├── adblock/                         # 请求级广告拦截策略、日志、合成响应
├── adguard/                         # AdGuard 规则文本解析
├── rules/                           # 规则模型、规则编译、索引、订阅导入、脚本规则
├── inject/                          # JS 加载、配置拼装、注入调度
├── element/                         # 元素选择器和手动屏蔽入口
├── video/                           # 网页视频桥接、全屏手势、原生播放器、队列、字幕、播放历史
├── functioncenter/                  # 功能中心页面、页面栈、动态 View 工厂、各设置/数据页面
├── download/                        # 下载发起、记录、状态同步、重试、取消、安全策略
├── localfiles/                      # 本地文件授权、目录枚举、文件打开/播放
├── settings/                        # 设置读写、默认设置恢复、站点权限会话存储
├── storage/                         # SharedPreferences 封装、收藏/历史存储和搜索
├── utils/                           # URL、媒体地址等纯工具函数
└── version/                         # 版本显示格式化
```

## 从一个功能向下追代码的方法

1. 先在“功能实现层级表”里找到功能行。
2. 从“第一入口”开始读，理解用户动作或系统回调从哪里进入。
3. 跳到“核心实现”，按函数注释阅读每个函数的功能和参数。
4. 如果函数读写数据，再跳到“数据/配置”列对应文件。
5. 最后读“相关测试”，确认哪些行为是不能随意改动的契约。

## 推荐阅读路线

### 第一次了解项目

| 顺序 | 文件 | 目的 |
| --- | --- | --- |
| 1 | `README.md` | 先了解已经实现的功能范围 |
| 2 | `app/src/main/AndroidManifest.xml` | 看 Activity、权限和网络配置 |
| 3 | `app/src/main/java/com/example/videobrowser/MainActivity.kt` | 看启动接线和模块关系 |
| 4 | `app/src/main/java/com/example/videobrowser/browser/BrowserManager.kt` | 看 WebView 如何创建和加载 |
| 5 | `app/src/main/java/com/example/videobrowser/browser/BrowserClient.kt` | 看页面加载、拦截、错误处理 |
| 6 | `app/src/main/java/com/example/videobrowser/browser/ChromeClient.kt` | 看网页权限、弹窗和全屏 |
| 7 | `app/src/main/java/com/example/videobrowser/inject/PageFeatureCoordinator.kt` | 看页面增强如何被配置和注入 |
| 8 | `app/src/main/assets/scripts/common.js` | 看网页内 JS 如何清理页面和控制视频 |
| 9 | `app/src/main/java/com/example/videobrowser/video/PlayerActivity.kt` | 看原生播放器 |
| 10 | `app/src/main/java/com/example/videobrowser/functioncenter/FunctionCenterController.kt` | 看功能中心如何打开和切页 |

### 想改浏览器加载问题

| 顺序 | 文件 |
| --- | --- |
| 1 | `MainActivity.kt` 的 `loadUrl`、导航相关 region |
| 2 | `browser/BrowserManager.kt` |
| 3 | `browser/BrowserClient.kt` |
| 4 | `browser/BrowserSessionController.kt` |
| 5 | `browser/BrowserErrorPage.kt` |
| 6 | `app/src/test/java/com/example/videobrowser/browser/*` |

### 想改广告拦截或页面净化

| 顺序 | 文件 |
| --- | --- |
| 1 | `inject/PageFeatureCoordinator.kt` |
| 2 | `adblock/AdBlockRequestPolicy.kt` |
| 3 | `rules/RuleEngine.kt` |
| 4 | `rules/RuleCompiler.kt` |
| 5 | `assets/scripts/common.js` |
| 6 | `element/ElementPickerController.kt` |
| 7 | `app/src/test/java/com/example/videobrowser/adblock/*`, `rules/*`, `inject/*` |

### 想改视频体验

| 顺序 | 文件 |
| --- | --- |
| 1 | `browser/VideoBrowserNativeBridge.kt` |
| 2 | `assets/scripts/common.js` |
| 3 | `site/SiteAdapterRegistry.kt` 和站点脚本 |
| 4 | `video/FullscreenVideoController.kt` |
| 5 | `video/FullscreenVideoGestureOverlay.kt` |
| 6 | `video/PlayerActivity.kt` |
| 7 | `video/PlaybackQueue.kt`, `video/PlaybackHistoryRepository.kt` |
| 8 | `app/src/test/java/com/example/videobrowser/video/*` |

### 想改功能中心或设置

| 顺序 | 文件 |
| --- | --- |
| 1 | `functioncenter/FunctionCenterController.kt` |
| 2 | `functioncenter/FunctionCenterPageHost.kt` |
| 3 | `functioncenter/FunctionCenterViewFactory.kt` |
| 4 | `functioncenter/FunctionCenterPages.kt` |
| 5 | 对应页面文件，例如 `BrowserSettingsPage.kt`, `DownloadsPage.kt`, `SavedPagesPage.kt` |
| 6 | `settings/SettingsManager.kt` 或对应 repository |
| 7 | `app/src/test/java/com/example/videobrowser/functioncenter/*` |

## 测试层级索引

| 测试目录 | 覆盖内容 | 修改代码后优先跑 |
| --- | --- | --- |
| `app/src/test/java/com/example/videobrowser/browser/` | WebView 客户端、标签页、权限、页面工具、导航策略 | 改 `browser/` 或 `MainActivity` 浏览器相关逻辑 |
| `app/src/test/java/com/example/videobrowser/browser/search/` | 搜索引擎、地址建议、首页搜索源横排隐藏约束 | 改 `browser/search/` |
| `app/src/test/java/com/example/videobrowser/adblock/` | 请求拦截、日志、合成响应 | 改 `adblock/` |
| `app/src/test/java/com/example/videobrowser/rules/` | 规则解析、编译、索引、订阅 | 改 `rules/` 或 `adguard/` |
| `app/src/test/java/com/example/videobrowser/inject/` | JS 注入配置和脚本加载 | 改 `inject/` 或 `assets/scripts/` |
| `app/src/test/java/com/example/videobrowser/video/` | 网页视频桥接、原生播放器、队列、手势、历史 | 改 `video/` 或视频 JS |
| `app/src/test/java/com/example/videobrowser/functioncenter/` | 功能中心页面契约和动作目录 | 改 `functioncenter/` |
| `app/src/test/java/com/example/videobrowser/download/` | 下载记录、安全、重试、取消、状态同步 | 改 `download/` |
| `app/src/test/java/com/example/videobrowser/settings/` | 设置读写和默认恢复 | 改 `settings/` |
| `app/src/androidTest/java/` | WebView/JS 在真实 Android 组件中的行为 | 改 WebView 注入、JS、页面交互时补跑 |

常用开发验证命令：

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:compileDebugAndroidTestKotlin
.\gradlew.bat :app:compileDebugKotlin
```

发布构建命令：

```powershell
.\gradlew.bat :app:assembleRelease
```
