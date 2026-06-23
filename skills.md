# VideoBrowser 后续开发约束

这份文件用于约束后续优化、重构和新增功能，目标是避免功能重新集中到少数大文件中，并避免已经实现的能力被重复实现。

## 必须先审查现有实现

每次修改前先搜索相关实现、调用点和契约测试。优先检查：

- `app/src/main/java/com/example/videobrowser/`
- `app/src/main/assets/scripts/`
- `app/src/test/java/com/example/videobrowser/`
- `docs/CODE_READING_HIERARCHY.md`
- `README.md`

如果目标能力已经存在，禁止重新实现一套。只能选择：

- 直接复用已有类、函数、脚本模块或资源。
- 在原有模块内补齐缺失参数、边界处理或测试。
- 抽出已有重复逻辑为共享模块后迁移调用方，并移除重复代码。

## 复用优先

能复用的代码只保留一份。发现两个或更多调用点实现同一类能力时，应优先抽取到已有职责最匹配的模块中。

常见复用归属：

- URL、媒体地址、文本格式化等纯函数放在 `utils/` 或已有专用工具类。
- 浏览器导航、权限、WebView、弹窗和站点安全逻辑放在 `browser/` 对应控制器。
- 功能中心页面、动作目录、页面栈和 View 创建放在 `functioncenter/` 对应页面或工厂。
- 设置读写、默认值和站点级设置放在 `settings/`。
- 下载记录、安全策略、重试和取消放在 `download/`。
- 原生播放器、网页视频桥接、队列、字幕和手势放在 `video/`。
- JavaScript 页面增强能力放在 `assets/scripts/` 中已有共享脚本或站点脚本。

禁止为了快速完成改动，在调用方复制已有逻辑、复制常量、复制选择器、复制弹窗创建流程或复制存储访问流程。

## 模块化边界

新增或优化内容必须按职责拆分。一个文件或类只承担一个清晰职责。

允许的集中：

- Assembly/Factory 只负责依赖装配和对象创建顺序。
- Controller 只负责一个业务流程的状态和事件协调。
- Repository/Store 只负责数据读写。
- Policy/Resolver 只负责规则判断。
- Dialog 工具只负责一种弹窗形态。
- Script module 只负责一类页面增强能力。

禁止的集中：

- 把多个无关功能继续塞进 `MainActivity.kt`、`BrowserManager.kt`、`FunctionCenterPages.kt`、`PlayerActivity.kt` 或 `common.js`。
- 新建一个宽泛的 `Common`、`Helper`、`Manager` 文件来收纳无关逻辑。
- 在装配类中加入业务判断、UI 细节或数据持久化。
- 在页面类中直接实现可复用的弹窗、Toast、URL 处理、存储解析或权限决策。

如果一个改动让文件职责变宽，必须同步拆出专门类或脚本模块。

## 已实现能力禁止再次实现

后续需求涉及以下内容时，必须先复用已有实现：

- Toast：复用 `ShortToast`。
- AlertDialog Builder 创建：复用 `AppDialog`。
- 确认弹窗、列表弹窗、输入弹窗：复用现有 dialog 工具。
- URL 和媒体地址判断：复用 `UrlUtils`、`MediaUrlUtils` 或现有策略类。
- 站点权限决策：复用现有权限控制器、Store 和 Decision Controller。
- JavaScript 注入和页面增强：复用 `ScriptLoader`、`JsInjector`、共享脚本模块和站点 adapter。

如果发现已有实现缺少能力，应扩展已有模块，而不是复制后改名。

## 测试和防回退

每次抽取或迁移复用逻辑后，都要补充或更新契约测试，固定以下事实：

- 共享能力只有一个 owner。
- 调用方不再包含被抽走的重复实现。
- 关键入口仍调用共享模块。
- 静态扫描能发现直接重复实现的回退。

适合使用 `projectFile(...).readText()` 的契约测试来锁定模块归属，也可以补充普通单元测试验证行为。

## 执行流程

每个阶段按以下顺序执行：

1. 搜索并列出现有相关实现。
2. 判断是否已有能力可复用。
3. 只在明确职责模块中修改或新增代码。
4. 迁移调用方并移除重复实现。
5. 更新契约测试或单元测试。
6. 运行定向测试和必要的全量测试。
7. 自动提交本阶段 Git commit。

完成标准：

- 没有新增重复实现。
- 可复用逻辑只有一个 owner。
- 新增代码落在明确职责模块中。
- 相关测试通过。
- 工作区已提交且无无关改动。
